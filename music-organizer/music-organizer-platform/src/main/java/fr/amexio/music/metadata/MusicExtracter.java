package fr.amexio.music.metadata;

//import org.alfresco.service.cmr.repository.ContentReader;
//import org.alfresco.transform.tika.metadata.extractors.TikaAudioMetadataExtractor;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.metadata.TikaCoreProperties;
//import org.apache.tika.metadata.XMPDM;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.stream.Collectors;

/**
 * ENDED UP NOT BEING USED WHATSOEVER, INGORE THIS CLASS
 * I keep it to have a trace of the code for now
 */
@Component
public class MusicExtracter //extends TikaAudioMetadataExtractor
{
	/*private static final Logger logger = LoggerFactory.getLogger(MusicExtracter.class);

	private static final String KEY_SONG_TITLE = "songTitle";
	private static final String KEY_ALBUM_TITLE = "albumTitle";
	private static final String KEY_YEAR_RELEASED = "yearReleased";
	private static final String KEY_TRACK_NUMBER = "trackNumber";
	private static final String KEY_DISC_NUMBER = "discNumber";
	private static final String KEY_GENRE = "genre";
	private static final String KEY_ARTIST = "artist";
	private static final String KEY_COMPOSER = "composer";

	private final Map<String, List<String>> composerMap = new HashMap<>();
	private final Map<String, List<String>> artistMap = new HashMap<>();

	public MusicExtracter()
	{
		super(logger);
		loadCsvFiles(); // Load CSV files at instantiation
	}*/

	/**
	 * Extracts metadata specific to music files.
	 *
	 * @param metadata   The Tika metadata object.
	 * @param properties The Alfresco properties map.
	 * @param headers    HTTP headers (unused).
	 * @return The updated properties map.
	 */
	// @Override
	/*protected Map<String, Serializable> extractSpecific(Metadata metadata, Map<String, Serializable> properties, Map<String, String> headers)
	{
		super.extractSpecific(metadata, properties, headers);

		// Extract basic music metadata
		extractBasicMetadata(metadata, properties);

		// Extract and process artists and composers
		processArtistsAndComposers(metadata, properties);

		return properties;
	}*/

	/**
	 * Extracts basic metadata like title, album, etc.
	 */
	/*private void extractBasicMetadata(Metadata metadata, Map<String, Serializable> properties)
	{
		putRawValue(KEY_SONG_TITLE, getTitle(metadata), properties);
		putRawValue(KEY_ALBUM_TITLE, metadata.get(XMPDM.ALBUM), properties);
		putRawValue(KEY_TRACK_NUMBER, metadata.get(XMPDM.TRACK_NUMBER), properties);
		putRawValue(KEY_DISC_NUMBER, metadata.get(XMPDM.DISC_NUMBER), properties);
		putRawValue(KEY_GENRE, metadata.get(XMPDM.GENRE), properties);
		putRawValue(KEY_YEAR_RELEASED, metadata.get(XMPDM.RELEASE_DATE), properties);
	}*/

	/**
	 * Extracts the song title from file metadata, falling back to the file name if no title is found
	 */
	/*private String getTitle(Metadata metadata)
	{
		String title = metadata.get(TikaCoreProperties.TITLE);
		
		if (title == null || title.isEmpty())
		{
			ContentReader reader = getContentReader(); // Placeholder, WIP
			
			if (reader != null)
			{
				String fileName = reader.getContentUrl(); // Retrieve the file name
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1); // Remove path
				return fileName.replaceFirst("[.][^.]+$", ""); // Remove extension
			}
		}
		
		return title;
	}*/

	/**
	 * Processes artists and composers from metadata and updates properties.
	 */
	/*private void processArtistsAndComposers(Metadata metadata, Map<String, Serializable> properties)
	{
		String artists = metadata.get(XMPDM.ARTIST);
		String composers = metadata.get(XMPDM.COMPOSER);

		Set<String> resolvedArtists = resolveNames(artists, artistMap);
		Set<String> resolvedComposers = resolveNames(composers, composerMap);

		if (!resolvedArtists.isEmpty())
			properties.put(KEY_ARTIST, new ArrayList<>(resolvedArtists));

		if (!resolvedComposers.isEmpty())
			properties.put(KEY_COMPOSER, new ArrayList<>(resolvedComposers));
	}*/

	/**
	 * Resolves names from metadata using the mapping from CSV.
	 */
	/*private Set<String> resolveNames(String rawNames, Map<String, List<String>> nameMap)
	{
		if (rawNames == null || rawNames.isEmpty())
			return Collections.emptySet();

		Set<String> resolvedNames = new HashSet<>();
		
		String[] names = rawNames.split(","); // Split names by comma
		for (String name : names)
		{
			name = name.trim();
			
			if (nameMap.containsKey(name))
				resolvedNames.addAll(nameMap.get(name)); // Adds all matching values
			else
				resolvedNames.add(name); // If no match, adds the raw name
		}
		
		return resolvedNames;
	}*/

	/**
	 * Loads CSV files for artist and composer mappings.
	 */
	/*private void loadCsvFiles()
	{
		try
		{
			composerMap.putAll(loadCsv("composer.csv"));
			artistMap.putAll(loadCsv("artist.csv"));
		} catch (IOException e)
		{
			logger.error("Error loading CSV files: ", e);
		}
	}*/

	/**
	 * Loads a CSV file into a mapping of name to aliases.
	 *
	 * @param fileName The CSV file name.
	 * 
	 * @return A map of names to their aliases.
	 * 
	 * @throws IOException If an error occurs while reading the file.
	 */
	/*private Map<String, List<String>> loadCsv(String fileName) throws IOException
	{
		Map<String, List<String>> map = new HashMap<>();
		
		try 
		{
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			String line;	
			while ((line = reader.readLine()) != null)
			{
				String[] entries = line.split(";");
				
				if (entries.length > 0)
				{
					String key = entries[0].trim();
					
					List<String> values = Arrays.stream(entries).map(String::trim).collect(Collectors.toList());
					map.put(key, values);
				}
			}
			
			return map;
		} catch (IOException e)
		{
			throw e;
		}
			
	}*/
}
