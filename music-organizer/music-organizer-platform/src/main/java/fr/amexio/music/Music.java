package fr.amexio.music;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;

public class Music
{
	private String link;
	private String title;
	private List<String> composers;
	private List<String> singers;
	private String albumName;
	private Integer trackNumber;
	private Integer discNumber;
	private List<String> genres;
	private Integer year;

	public Music(String link)
	{
		setLink(link);
	}
	
	// Link

	public String getLink()
	{
		return link;
	}
	
	public void setLink(String link)
	{
		this.link = link;
	}

	// Title
	
	public boolean hasTitle()
	{
		return (stringIsValid(title));
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		if (stringIsValid(title))
			this.title = title.trim();
	}

	public void removeTitle()
	{
		this.title = null;
	}
	
	// Artists
	
	public boolean hasComposers()
	{
	    return (listIsValid(composers));
	}
	
	public List<String> getComposers()
	{
	    return composers;
	}
	
	public void addComposer(String composer)
	{
	    if (stringIsValid(composer))
	    {
	    	if (!listIsValid(composers))
	    		composers = new ArrayList<String>();
	    	
	        if (!composers.contains(composer.trim()))
	            composers.add(composer.trim());
	    }
	}

	public void removeComposer(String composer)
	{
	    if (listIsValid(composers))
	    	composers.remove(composer.trim());
	}
	
	public boolean hasSingers()
	{
	    return (listIsValid(singers));
	}
	
	public List<String> getSingers()
	{
	    return singers;
	}
	
	public void addSinger(String singer)
	{
	    if (stringIsValid(singer))
	    {
	    	if (!listIsValid(singers))
	    		singers = new ArrayList<String>();
	    	
	        if (!singers.contains(singer.trim()))
	            singers.add(singer.trim());
	    }
	}

	public void removeSinger(String singer)
	{
	    if (listIsValid(singers))
	        singers.remove(singer.trim());
	}
	
	public String getArtists()
	{
		StringBuilder artists = new StringBuilder();
		
		if(listIsValid(singers))
			for(String artist : singers)
			{
				artists.append(artist);
				artists.append(',');
			}

		if(listIsValid(composers))
			for(String artist : composers)
			{
				artists.append(artist);
				artists.append(',');
			}
		
		artists.deleteCharAt(artists.length()-1); // removes the comma a the end
		
		return artists.toString();
	}

	// Album Title
	
	public boolean hasAlbumName()
	{
		return (stringIsValid(albumName));
	}

	public String getAlbumName()
	{
		return albumName;
	}

	public void setAlbumName(String albumName)
	{
		if (stringIsValid(albumName))
			this.albumName = albumName.trim();
	}

	public void removeAlbumName()
	{
		this.albumName = null;
	}

	// Track Number
	
	public boolean hasTrackNumber()
	{
		return trackNumber != null;
	}

	public Integer getTrackNumber()
	{
		return trackNumber;
	}

	public void setTrackNumber(Integer trackNumber)
	{
		this.trackNumber = trackNumber;
	}

	public void removeTrackNumber()
	{
		setTrackNumber(null);
	}

	// Disc Number
	
	public boolean hasDiscNumber()
	{
		return discNumber != null;
	}

	public Integer getDiscNumber()
	{
		return discNumber;
	}

	public void setDiscNumber(Integer discNumber)
	{
		this.discNumber = discNumber;
	}

	public void removeDiscNumber()
	{
		setDiscNumber(null);
	}

	// Genres
	
	public boolean hasGenres()
	{
		return (listIsValid(genres));
	}

	public List<String> getGenres()
	{
		return genres;
	}

	public String getGenresAsString()
	{
		StringBuilder sb = new StringBuilder();
		
		if(listIsValid(genres))
			for(String genre : genres)
			{
				sb.append(genre);
				sb.append(',');
			}
		
		sb.deleteCharAt(sb.length()-1); // removes the comma a the end
		
		return sb.toString();
	}

	public void addGenre(String genre)
	{
		if (stringIsValid(genre))
		{
			if (!listIsValid(genres))
				genres = new ArrayList<>();
			
			if (!genres.contains(genre))
				genres.add(genre.trim());
		}
	}

	public void removeGenre(String genre)
	{
	    if (listIsValid(genres))
	        genres.remove(genre.trim());
	}

	// Year
	
	public boolean hasYear()
	{
		return year != null;
	}

	public Integer getYear()
	{
		return year;
	}

	public void setYear(Integer year)
	{
		this.year = year;
	}

	public void removeYear()
	{
		setYear(null);
	}
	
	// Process
	
	protected boolean stringIsValid(String test)
	{
		return (test != null && !test.trim().isEmpty());
	}
	
	protected boolean listIsValid(List<String> test)
	{
		return (test != null && !test.isEmpty());
	}
}
