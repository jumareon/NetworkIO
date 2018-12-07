package lib.basicFrm.rtmp;

public abstract class RTMPPublisher
{
	public static class Event
	{
		private final int conversationId;
		
		public Event(final int $conversationId)
		{
			this.conversationId = $conversationId;
		}
		
		public int getConversationId()
		{
			return this.conversationId;
		}
	}
	
	public RTMPPublisher()
	{
		
	}
}
