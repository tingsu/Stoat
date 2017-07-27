public class InnerClassTest {
	public static void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
		test5();
	}
	private static void test1() {
		A a = new A();
		a.m();
	}
	private static void test2() {
		A a = new A();
		a.n();
	}
	private static void test3() {
		A a = new A();
		a.p();
	}
	// Anonymous interface class
	private static void test4() {
		I i = new I() {
			public void m() {
			}
		};
		i.m();
	}
	// Anonymous class
	private static void test5() {
		B b = new B() {
			public void n() {
			}
		};
		b.n();
	}
	// Local class
	private static void test6() {
		A a = new A();
		a.o();
	}
	// Anonymous class
	private static void test7() {
		C c = new C();
		c.m();
	}
}

class A {
	void m() {
		// Local classes
		class LocalA {
			void localM() {
				B b = new B();
				b.m();
			}
		}
		class LocalB {
			// Redeclaration of local class, ok if there is an 
			// other enclosing local class in between
			class LocalA {
				void m() {
				}
			}
			void m() {
				LocalA a = new LocalA();
				a.m();
			}
		}
		LocalA a = new LocalA();
		a.localM();
		LocalB b = new LocalB();
		b.m();
	}

	void o() {
		class LocalInO extends B {
			void n() {
			}
		}
		B o = new LocalInO();
		o.n();
	}

	void n() {
		InnerA a = new InnerA(); 
		a.m();
	}

	void p() {
		InnerA a = new InnerA();
		a.n();
	}

	class InnerA extends B {
		void m() {
		}
	}
}

class B {
	void m() {
	}
	void n() {
	}
}

interface I {
	void m();
}

class C {
	// This anonymous class is accessible in m() via the field i
	I i = new I() {
		public void m() {
		}
	};

	void m() {
		i.m();
	}
}

