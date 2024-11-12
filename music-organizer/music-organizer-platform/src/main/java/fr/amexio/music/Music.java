package fr.amexio.music;

public class Music
{
	private String link;
	private String title;

	public Music(String link, String title)
	{
		setLink(link);
		setTitle(title);
	}
	
	public Music(String link)
	{
		this(link, null);
	}
	
	public boolean hasTitle()
	{
		return (title != null && !title.equals(""));
	}

	public String getLink()
	{
		return link;
	}
	
	public void setLink(String link)
	{
		this.link = link;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title.trim();
	}
}
