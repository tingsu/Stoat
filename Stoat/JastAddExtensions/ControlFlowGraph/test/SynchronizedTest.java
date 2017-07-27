package test;

public class SynchronizedTest {
	void v(String s){
		synchronized (s="aaa") { s = "bbb"; }
	}
	
	public synchronized void increment(int a) {
		a++;
    }


}
