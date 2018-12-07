package lib.middleFrm.stream;

public enum PublishType
{
	LIVE,
	APPEND,
	RECORD;
	
	public String asString()
	{
		return this.name().toLowerCase();
	}
	
	public static PublishType parse(final String raw)
	{
		return PublishType.valueOf(raw.toUpperCase());
	}
}
