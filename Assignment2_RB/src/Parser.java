import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class Parser {

	File file;
	BufferedReader br;
	
	public Parser(String fileLocation) throws FileNotFoundException {
		file = new File(fileLocation);
		br = new BufferedReader(new FileReader(file));
	}
	
	private ArrayList<String> parseIntoLines() throws IOException{
		ArrayList<String> lines = new ArrayList<String>();
		String st;
		while((st = br.readLine()) != null) {
			lines.add(st);
		}
		return lines;
	}
	
	public void GenerateCHRFile() throws IOException {
		ArrayList<String> Rules = parseIntoLines();
		PrintWriter writer = new PrintWriter("CHR^rp.pl", "UTF-8");
		writer.println(":- use_module(library(chr)).");
		writer.println(":- chr_constraint start/0, conflictdone/0,fire/0, id/1, history/1,");
		String charactersDefinition = "";
		ArrayList<String> charactersSoFar = new ArrayList<String>();
		for(int i = 0;i < Rules.size();i++) {
			String[] headPreConditions;
			if(Rules.get(i).contains("<=>"))
				headPreConditions = Rules.get(i).split("<=>");
			else
				headPreConditions = Rules.get(i).split("==>");
			String[] conditions = headPreConditions[0].split(",");
			for(int j = 0; j < conditions.length;j++) {
				System.out.println(conditions[j]);
				if(!charactersSoFar.contains(conditions[j])) {
					charactersSoFar.add(conditions[j]);
					charactersDefinition = charactersDefinition + conditions[j] + "/1,";
				}
			}
		}
		writer.println(charactersDefinition.substring(0,charactersDefinition.length() - 1) + ".");
		writer.println();
		GenerateIDRules(charactersSoFar, writer);
		writer.println();
		GenerateMatchRules(Rules, writer);
		writer.println();
		writer.println("start <=> conflictdone.");
		writer.println();
		writer.println();
		
		writer.println("history(L),conflictdone\\match(R,_,IDs) <=>");
		writer.println("member((R,FIDs),L),sort(IDs,II),sort(FIDs,II)|true.");
		writer.println();
		
		writer.println("conflictdone,match(_,P1,_)\\ match(_,P2,_)");
		writer.println("<=> P1<P2 | true.");
		writer.println();
		
		writer.println("conflictdone ,match(_,P1,_) \\ match(_,P2,_)");
		writer.println("<=> P1 = P2, A is random(2), A = 1 | true.");
		writer.println();
		writer.println();
		writer.println("conflictdone <=> fire.");
		writer.println();
		
		//TODO Firing phase
		
		writer.close();
		 
	}
	
	private void GenerateIDRules(ArrayList<String> charactersSoFar, PrintWriter writer) {
		for(int i = 0; i < charactersSoFar.size(); i++) {
			writer.println("id(I), " + charactersSoFar.get(i) + " <=> " + charactersSoFar.get(i) + "(I), I1 is I+1,id(I1).");
		}
	}
	
	private void GenerateMatchRules(ArrayList<String> Rules, PrintWriter writer) {
		for(int i = 0; i < Rules.size(); i++) {
			String ruleHead;
			String rulePriority = Rules.get(i).split("pragma")[1];
			if(Rules.get(i).contains("<=>"))
				ruleHead = Rules.get(i).split("<=>")[0];
			else
				ruleHead = Rules.get(i).split("==>")[0];
			String[] conditions = ruleHead.split(",");
			String ruleToPrint = "";
			ArrayList<String> ruleIDs = new ArrayList<String>();
			for(int j = 0; j < conditions.length;j++) {
				if(j == 0) {
					ruleToPrint += "start, " + conditions[j] + "(ID) ";
					ruleIDs.add("ID");
				}
				else {
					ruleToPrint += ", " + conditions[j] + "(ID" + j + ")";
					ruleIDs.add("ID" + j);
				}
			}
			ruleToPrint += " ==> match(r" + i + "," + rulePriority + "," + ruleIDs + ").";
			writer.println(ruleToPrint);
		}
	}
}