package fr.amexio.music.metadata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

public class MusicExtracter extends AbstractMappingMetadataExtracter
{
	private static final String dataSource = "Data Dictionary/Music/";
	
    private static final String[] SUPPORTED_MIMETYPES = { "audio/mpeg", "audio/flac", "audio/wav", "audio/midi", "audio/x-midi",
    		"audio/ogg", "audio/x-wav", "audio/x-ms-wma", "audio/aac", "audio/mp4", "audio/x-aiff", "audio/aiff"};

    private Map<String, List<String[]>> knownComposers;
    private Map<String, List<String[]>> knownSingers;

    public String[] getSupportedMimeTypes()
    {
        return SUPPORTED_MIMETYPES;
    }

    protected Map<String, Set<QName>> getMapping()
    {
        Map<String, Set<QName>> mapping = new HashMap<>();
        
        mapping.put("title", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "songTitle")));
        mapping.put("album", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "album")));
        mapping.put("artist", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "artist")));
        mapping.put("genre", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "genre")));
        mapping.put("year", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "year")));
        mapping.put("track_number", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "trackNumber")));
        mapping.put("disc_number", Set.of(QName.createQName("http://www.amexio.fr/model/music/1.0", "discNumber")));
        
        return mapping;
    }

    protected void extractSpecific(ContentReader reader, Map<String, Serializable> destination) throws Throwable
    {
        if (knownComposers == null || knownSingers == null)
            extractCsvData();

        Metadata tikaMetadata = extractMetadataWithTika(reader);
        copyMetadata(tikaMetadata, destination);

        processArtists(destination, "composer", knownComposers, "ax:composer", "ax:composerName");
        processArtists(destination, "artist", knownSingers, "ax:singer", "ax:singerName");
    }

    private Metadata extractMetadataWithTika(ContentReader reader) throws Exception
    {
        Metadata metadata = new Metadata();
        BodyContentHandler handler = new BodyContentHandler();
        AutoDetectParser parser = new AutoDetectParser();

        try (InputStreamReader isr = new InputStreamReader(reader.getContentInputStream()))
        {
            parser.parse(reader.getContentInputStream(), handler, metadata);
        }

        return metadata;
    }

    private void copyMetadata(Metadata source, Map<String, Serializable> destination)
    {
        destination.put("title", source.get("title") != null ? source.get("title") : source.get("name").replaceAll("\\.[^\\.]+$", ""));
        destination.put("album", source.get("album"));
        destination.put("artist", source.get("artist"));
        destination.put("genre", source.get("genre"));
        destination.put("year", parseInteger(source.get("year")));
        destination.put("track_number", parseInteger(source.get("track_number")));
        destination.put("disc_number", parseInteger(source.get("disc_number")));
    }

    private void processArtists(Map<String, Serializable> destination, String key,
    Map<String, List<String[]>> csvData, String aspect, String property)
    {
        if (destination.containsKey(key) && destination.get(key) != null)
        { // Fetches data on field, values split by commas
            String[] values = destination.get(key).toString().split(",");
            List<String> matches = new ArrayList<>(); // normalized values of what's been found in the CSV
            List<String> unmatched = new ArrayList<>(); // what wasn't supported by the SCV

            for (String value : values) // For each Value on the File's field
            {
                boolean matched = false; // reset
                String normalizedValue = normalize(value); // To have more flexibility

                // CSV Search
                for (Map.Entry<String, List<String[]>> entry : csvData.entrySet()) // for each row (raw data)
                {
                    for (String[] row : entry.getValue()) // row data as strings
                    	for (String name : row) // for each name in the row
                    	{
	                        String normalizedRow = normalize(row[0]);
	                        
	                        if (normalize(name).equals(normalizedValue)) // if a name in the row is matching the value...
	                        {
	                            if (!matches.contains(row[0])) // ... and the first value in the row isn't already in the list...
	                                matches.add(row[0]); // ... then it adds it
	                            
	                            matched = true;
	                            break;
	                        }
                    	}
                    
                    if (matched) break;
                }
               

                if (!matched)
                    unmatched.add(value); // No Match Found
            }

            // Add the Matches to the Node
            if (!matches.isEmpty())
            {
                destination.put(property, matches.toArray(new String[0]));

                addAspectIfNecessary(destination, aspect);
            }

            // When there's no match (for logs)
            if (!unmatched.isEmpty()) 
                System.out.println("No Mach found for " + key + ": " + unmatched);
        }
    }

    private void addAspectIfNecessary(Map<String, Serializable> destination, String aspect)
    {
        List<String> aspects = (List<String>) destination.getOrDefault("aspects", new ArrayList<>());
        if (!aspects.contains(aspect)) {
            aspects.add(aspect);
            destination.put("aspects", (Serializable) aspects);
        }
    }

    private void extractCsvData()
    {
        knownComposers = loadCsvFile(dataSource + "composers.csv");
        knownSingers = loadCsvFile(dataSource + "vocal_artists.csv");
    }

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

    private String normalize(String value)
    {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawMetadata = new HashMap<>();
        Metadata tikaMetadata = extractMetadataWithTika(reader);
        copyMetadata(tikaMetadata, rawMetadata);
        return rawMetadata;
    }
}
