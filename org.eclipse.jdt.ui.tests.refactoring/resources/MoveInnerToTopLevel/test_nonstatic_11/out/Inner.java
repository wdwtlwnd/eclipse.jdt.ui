package p;
class Inner {
	private A a;

	/**
	 * @param A
	 */
	Inner(A a) {
		this.a= a;
		// TODO Auto-generated constructor stub
	}

	void f(){
		new Inner(this.a);
	}
}