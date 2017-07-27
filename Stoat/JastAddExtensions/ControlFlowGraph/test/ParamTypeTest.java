public class ParamTypeTest {
	public static void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
	}
	private static void test1() {
		K<D> a = new K<D>();
		a.m(new D());		
	}
	private static void test2() {
		K<D> a = new B<D>();
		a.m(new D());		
	}
	private static void test3() {
		B<D> a = new B<D>();
		a.m(new D());		
	}

	private static void test4() {
		E<D,C> a = new E<D,C>();
		a.m(new D());		
	}
	private static void test5() {
		E<D,C> a = new E<D,C>();
		a.n(new C());
	}
	private static void test6() {
		K<D> a = new E<D,C>();
		a.m(new D());
	}

}

class A {

}

class C extends A {
}

class D {
	public void m() {
	}
}

class K<T> {
	public void m(T t) {
	}
}

class B<T> extends K<T> {
	public void m(T t) {
	}
}

class E<T,Q> extends K<T> {
	public void m(T t) {
	}
	public void n(Q q) {
	}
}


