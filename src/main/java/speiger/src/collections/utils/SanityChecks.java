package speiger.src.collections.utils;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * Helper class that provides functions that are shared within the library.
 * On top of that it provides some helper functions that allow control of some internals of the Library
 */
public class SanityChecks
{
	public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	private static ForkJoinPool WORK_POOL = ForkJoinPool.commonPool();
	private static ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);
	
	/**
	 * Internal function to cast a Integer to a Byte
	 * @param value that should be turned into a byte
	 * @return the value as a byte
	 * @throws IllegalStateException if the value does not fit within a byte
	 */
	public static byte castToByte(int value) {
		if(value > Byte.MAX_VALUE || value < Byte.MIN_VALUE) throw new IllegalStateException("Value ["+value+"] out of Byte[-128,127] range");
		return (byte)value;
	}
	
	/**
	 * Internal function to cast a Integer to a Short
	 * @param value that should be turned into a short
	 * @return the value as a short
	 * @throws IllegalStateException if the value does not fit within a short
	 */	
	public static short castToShort(int value) {
		if(value > Short.MAX_VALUE || value < Short.MIN_VALUE) throw new IllegalStateException("Value ["+value+"] out of Short[-32768,32767] range");
		return (short)value;
	}
	
	/**
	 * Internal function to cast a Integer to a Character
	 * @param value that should be turned into a char
	 * @return the value as a char
	 * @throws IllegalStateException if the value does not fit within a char
	 */
	public static char castToChar(int value) {
		if(value > Character.MAX_VALUE || value < Character.MIN_VALUE) throw new IllegalStateException("Value ["+value+"] out of Character[0,65535] range");
		return (char)value;
	}
	
	/**
	 * Internal function to cast a Double to a Double
	 * @param value that should be turned into a float
	 * @return the value as a float
	 * @throws IllegalStateException if the value does not fit within a float
	 */
	public static float castToFloat(double value) {
		if(Double.isNaN(value)) return Float.NaN;
		if(Double.isInfinite(value)) return value > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
		if(value < -Float.MAX_VALUE || value > Float.MAX_VALUE) throw new IllegalStateException("Value ["+value+"] out of Float range");
		return (float)value;
	}
	
	/**
	 * Internal function that checks if the given array-size is big enough for the access.
	 * @param arraySize the size of the Array
	 * @param offset the offset of the access
	 * @param accessSize the lenght of the access
	 * @throws IllegalStateException if offset or accessSize is negative or the range goes out of the array-size
	 */
	public static void checkArrayCapacity(int arraySize, int offset, int accessSize) {
		if(offset < 0) throw new IllegalStateException("Offset is negative ("+offset+")");
		else if(accessSize < 0) throw new IllegalStateException("Size is negative ("+accessSize+")");
		else if(arraySize < offset + accessSize) throw new IndexOutOfBoundsException("Index (" + (offset + accessSize) + ") is not in size (" + arraySize + ")");
	}
	
	/**
	 * Returns if the current thread-pool can handle multi-threading tasks
	 * @return true if the threadcount is bigger the 1
	 */
	public static boolean canParallelTask() {
		return WORK_POOL.getParallelism() > 1;
	}
	
	/**
	 * Helper method to start a ForkJoinTask. This method will await the finalization of said task
	 * @param task the Task to invoke
	 */
	public static <T> void invokeTask(ForkJoinTask<T> task) {
		WORK_POOL.invoke(task);//TODO implement a way to decide if the task should be awaited or not. Maybe even on a per Thread basis.
	}
	
	/**
	 * Helper method to start a ForkJoinTask. This method will not await the finalization of said task
	 * @param task the Task to invoke
	 */
	public static <T> void invokeAsyncTask(ForkJoinTask<T> task) {
		WORK_POOL.execute(task);
	}
	
	/**
	 * Helper method to control what ForkJoinPool is being used for any given task.
	 * @Note this method is not thread-save. It is only there to provide control over how Library specific Threaded tasks are handled.
	 * @param pool The ForkJoinPool that should receive the tasks. If null {@link ForkJoinPool#commonPool()} is set instead
	 */
	public static void setWorkPool(ForkJoinPool pool) {
		WORK_POOL = pool == null ? ForkJoinPool.commonPool() : pool;
	}
	
	public static Random getRandom() {
		return RANDOM.get();
	}
}
