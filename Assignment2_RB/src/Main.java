import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		Parser chrParser = new Parser("input_pchr.pl");
		chrParser.GenerateCHRFile();
	}

}
