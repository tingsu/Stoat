public class CallGraphTest {
	public static void main(String[] args) {
//		A a = new A();
//		B b = new B();
//		a.b(0);
//		b.b(1);
		A a = new C();
		a.c();
		I i = new B();
		i.m();
	}
}

class A implements J {
	public A() {
		this(0);
	}
	public A(int a) {
		b(a);
	}
	public void b(int a) {
	}
	public void c() {
	}

	public void c(int i) {
	}
	public void m() {
	}
}

class B extends A {
	public B() {
		super(1);
	}
	public void b(int a) {
	}
	public void m() {
	}
}
class C extends B implements J {
	public void c() {
	}
	public void m() {
	}
}
class P implements J {

	static int b = staticM();
	static {
		staticM();
	}
	{
		m();
	}
	int a = n();

	public void m() {
	}
	public int n() {
		return 0;
	}
	public static int staticM() {
		return 0;
	}
}

interface I {
	public void m();
}
interface J extends I {
	public void m();
}
