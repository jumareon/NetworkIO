package lib.basicFrm.utils;

import java.util.Arrays;

public class ValueToEnum<T extends Enum<T> & ValueToEnum.IntValue>
{
	public static interface IntValue
	{
		int intValue();
	}
	
	@SuppressWarnings("rawtypes")
	private final Enum[] lookupArray;
	private final int maxIndex;
	
	public ValueToEnum(final T[] enumValues)
	{
		final int[] lookupIndexes = new int[enumValues.length];
		for( int i = 0; i < enumValues.length; i++ )
		{
			lookupIndexes[i] = enumValues[i].intValue();
		}
		
		Arrays.sort(lookupIndexes);
		
		maxIndex = lookupIndexes[lookupIndexes.length - 1];
		lookupArray = new Enum[maxIndex + 1];
		for( final T t : enumValues )
		{
			lookupArray[t.intValue()] = t;
		}
	}
	
	@SuppressWarnings("unchecked")
	public T valueToEnum(final int i)
	{
		final T t;
		try
		{
			t = (T) lookupArray[i];
		}
		catch (Exception e)
		{
			throw new RuntimeException(getErrorLogMessage(i) + ", " + e);
		}
		
		if (t == null)
		{
			throw new RuntimeException(getErrorLogMessage(i) + ", no match found in lookup");
		}
		
		return t;
	}
	
	public int getMaxIndex()
	{
		return maxIndex;
	}
	
	private String getErrorLogMessage(final int i)
	{
		return "bad value / byte: " + i + " (hex: " + StringUtils.toHex((byte) i) + ")";
	}
}
