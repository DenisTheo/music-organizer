package fr.amexio.music.metadata;

import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;

/**
 * Custom metadata extractor for processing music metadata. 
 * It extracts and maps composers and singers metadata to custom Alfresco aspects.
 */
public class MusicExtracter extends AbstractMappingMetadataExtracter
{
    private static final String DATA_SOURCE = "Data Dictionary/Music/";

    // Supported MIME types for music files
    private static final Set<String> SUPPORTED_MIMETYPES = Set.of(
        "audio/mpeg", "audio/flac", "audio/wav", "audio/x-wav", 
        "audio/midi", "audio/x-midi", "audio/ogg", "audio/x-ms-wma", 
        "audio/aac", "audio/mp4", "audio/x-aiff", "audio/aiff");

    // CSV data caches for composers and singers
    private Map<String, List<String[]>> knownComposers;
    private Map<String, List<String[]>> knownSingers;

    /**
     * Constructor initializing supported MIME types.
     */
    public MusicExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }

    /**
     * Extracts specific metadata and processes it into the destination map.
     * 
     * @param reader The content reader for accessing the file content.
     * @param destination The destination map to store extracted metadata.
     * 
     * @throws Throwable If any error occurs during extraction.
     */
    protected void extractSpecific(ContentReader reader, Map<String, Serializable> destination) throws Throwable
    {
        if (knownComposers == null || knownSingers == null)
            extractCsvData();

        Metadata tikaMetadata = extractMetadataWithTika(reader);
        copyMetadata(tikaMetadata, destination);

        processArtists(destination, "composer", knownComposers, "ax:composer", "ax:composerName");
        processArtists(destination, "artist", knownSingers, "ax:singer", "ax:singerName");
    }

    /**
     * Extracts metadata using Apache Tika.
     * 
     * @param reader The content reader for accessing the file content.
     * 
     * @return The extracted metadata.
     * 
     * @throws Exception If any error occurs during metadata extraction.
     */
    private Metadata extractMetadataWithTika(ContentReader reader) throws Exception
    {
        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler();
        AutoDetectParser parser = new AutoDetectParser();

        try (InputStreamReader isr = new InputStreamReader(reader.getContentInputStream()))
        {
            parser.parse(reader.getContentInputStream(), handler, metadata);
        } catch (Exception e)
        {
        	throw e;
        }

        return metadata;
    }

    /**
     * Copies metadata from a source to a destination map.
     * 
     * @param source The source metadata.
     * @param destination The destination map to store metadata.
     */
    private void copyMetadata(Metadata source, Map<String, Serializable> destination)
    {
    	// Generates a title from file name if none is found in the file's metadata
        destination.put("title", source.get("title") != null ? source.get("title") : source.get("name").replaceAll("\\.[^\\.]+$", ""));
        destination.put("album", source.get("album"));
        destination.put("artist", source.get("artist"));
        destination.put("genre", source.get("genre"));
        destination.put("year", parseInteger(source.get("year")));
        destination.put("track_number", parseInteger(source.get("track_number")));
        destination.put("disc_number", parseInteger(source.get("disc_number")));
    }

    /**
     * Processes artist metadata and adds it to the destination map.
     * 
     * @param destination The destination map for metadata.
     * @param key The metadata key to process (e.g., "composer").
     * @param csvData The CSV data containing known artist information.
     * @param aspect The aspect to add if matches are found.
     * @param property The property to store matched artist names.
     */
    private void processArtists(Map<String, Serializable> destination, String key, Map<String, List<String[]>> csvData, String aspect, String property)
    {
        if (destination.containsKey(key) && destination.get(key) != null)
        {
            String[] values = destination.get(key).toString().split(",");
            List<String> matches = new ArrayList<>();
            List<String> unmatched = new ArrayList<>();

            for (String value : values)
            {
                String normalizedValue = normalize(value);
                boolean matched = false;

                for (List<String[]> rows : csvData.values())
                {
                    for (String[] row : rows)
                    {
                        for (String name : row)
                        {
                            if (normalize(name).equals(normalizedValue))
                            {
                                if (!matches.contains(row[0]))
                                    matches.add(row[0]);

                                matched = true;
                                break;
                            }
                        }
                        
                        if (matched)
                            break;
                    }
                    
                    if (matched)
                        break;
                }

                if (!matched)
                    unmatched.add(value);
            }

            if (!matches.isEmpty())
            {
                destination.put(property, matches.toArray(new String[0]));
                addAspectIfNecessary(destination, aspect);
            }

            if (!unmatched.isEmpty())
            {
                System.out.println("No match found for " + key + ": " + unmatched);
            }
        }
    }

    /**
     * Adds an aspect to the destination map if it does not already exist.
     * 
     * @param destination The destination map for metadata.
     * @param aspect The aspect to add.
     */
    @SuppressWarnings("unchecked") // Not the best
	private void addAspectIfNecessary(Map<String, Serializable> destination, String aspect)
    {
        List<String> aspects = new ArrayList<>();

        if (destination.get("aspects") instanceof List<?>)
        {
            // List<String> Type Check
            List<?> tempList = (List<?>) destination.get("aspects");

            if (tempList.stream().allMatch(item -> item instanceof String))
                aspects = (List<String>) tempList;
            else
                throw new IllegalStateException("The 'aspects' key does not contain a List<String>");
        }

        // Adds Aspect if needed
        if (!aspects.contains(aspect))
        {
            aspects.add(aspect);
            destination.put("aspects", (Serializable) aspects);
        }
    }

    /**
     * Loads and normalizes CSV data from a file.
     * 
     * @param filePath The path to the CSV file.
     * 
     * @return A map containing the loaded CSV data.
     */
    private Map<String, List<String[]>> loadCsvFile(String filePath)
    {
        Map<String, List<String[]>> data = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filePath))))
        {
            for (String line; (line = br.readLine()) != null;)
            {
                String[] parts = line.split(",");
                String key = normalize(parts[0]);
                data.putIfAbsent(key, new ArrayList<>());
                data.get(key).add(parts);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * Extracts all raw metadata from a content reader.
     * 
     * @param reader The content reader.
     * 
     * @return A map of raw metadata.
     * 
     * @throws Throwable If any error occurs during extraction.
     */
    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawMetadata = new HashMap<>();

        Metadata tikaMetadata = extractMetadataWithTika(reader);

        for (String name : tikaMetadata.names())
        {
            String value = tikaMetadata.get(name);

            if (value != null)
                rawMetadata.put(name, value);
        }

        return rawMetadata;
    }

    /**
     * Extracts CSV data for composers and singers.
     */
    private void extractCsvData()
    {
        knownComposers = loadCsvFile(DATA_SOURCE + "composers.csv");
        knownSingers = loadCsvFile(DATA_SOURCE + "vocal_artists.csv");
    }

    /**
     * Normalizes a string by converting it to lowercase and removing non-alphanumeric characters.
     * 
     * @param value The string to normalize.
     * 
     * @return The normalized string.
     */
    private String normalize(String value)
    {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    /**
     * Parses a string into an integer, returning -1 if the parsing fails.
     * 
     * @param value The string to parse.
     * 
     * @return The parsed integer or -1 if parsing fails.
     */
    private int parseInteger(String value)
    {
        try
        {
            return Integer.parseInt(value);
        } catch (NumberFormatException e)
        {
            return -1;
        }
    }

    /**
     * Returns the supported MIME types.
     * 
     * @return A set of supported MIME types.
     */
    //@Override
    public Set<String> getSupportedMimeTypes()
    {
        return SUPPORTED_MIMETYPES;
    }
}
