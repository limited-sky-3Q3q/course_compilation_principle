package lexer;

public class STR extends Token {
    public String value = "";

	public STR(String v) {
		super(Tag.STR);
		value = v;
	}

	public String toString() {
		return "" + value;
	}
}
