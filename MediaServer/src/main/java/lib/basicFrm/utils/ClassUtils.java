package lib.basicFrm.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassUtils
{
	/**
	 * ���ڿ��� ���� Ư�� Ŭ������ Method �� ȣ��
	 * @param target		- �ش� �޼ҵ带 ������ Class Target
	 * @param methodName	- Method Name
	 * @param params		- Parameter Object List
	 * @return Object Type�� ���� �����ϸ� ���� ���ش�
	 */
	public static Object invoke( Object target, String methodName, Object[] params )
	{
		Method[] methods = target.getClass().getMethods();
		
		for( int i=0; i < methods.length; i++ )
		{
			if( methods[i].getName().equals( methodName ) )
			{
				try
				{
					if( methods[i].getReturnType().getName().equals( "void" ) )
					{
						methods[i].invoke( target, params );
						break;
					}
					else
					{
						return methods[i].invoke( target, params );
					}
				}
				catch(IllegalAccessException iae)
				{
					System.err.println("ClassUtils Error [Method Invoke] Exception : " + iae);
				}
				catch( InvocationTargetException ite )
				{
					System.err.println("ClassUtils Error [Method Invoke] Exception : " + ite);
				}
				catch( Exception e )
				{
					System.err.println("ClassUtils Error [Method Invoke] Exception : " + e);
				}
			}
		}
		
		return null;
	}
}
