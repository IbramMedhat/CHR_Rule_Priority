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
			if(st != "" && st != " " && st != "\n" && st.length() > 1) {
				lines.add(st);
			}
		}
		return lines;
	}
	
	public void GenerateCHRFile() throws IOException {
		
		ArrayList<String> Rules = parseIntoLines();
		
		if(Rules.size() > 0) {
			
			for(int i = 0; i < Rules.size(); i++){
				Rules.set(i, Rules.get(i).replaceAll("\\s+", "")) ;
				System.out.println(Rules.get(i));
			}
			PrintWriter writer = new PrintWriter("CHR^rp.pl", "UTF-8");
			
			// Adding library chr
			writer.println(":- use_module(library(chr)).");
			// Adding the chr constraints commen between all rules 
			writer.println(":- chr_constraint start/0, conflictdone/0,fire/0, id/1, history/1, match/3,");
			String charactersDefinition = "";
			ArrayList<String> charactersSoFar = new ArrayList<String>();
			ArrayList<String> actionsSoFar = new ArrayList<String>();
			
			for(int i = 0;i < Rules.size();i++) {
				String[] headPreConditions;
				String[] actions;
				if(Rules.get(i).contains("<=>")) {
					headPreConditions = Rules.get(i).split("<=>");
				}
				else {
					headPreConditions = Rules.get(i).split("==>");
				}
				String[] conditions = headPreConditions[0].split(",");	
				for(int j = 0; j < conditions.length;j++) {
					System.out.println(conditions[j]);
					
					if(conditions[j].contains("\\")) {
	
	
						
						if(!charactersSoFar.contains(conditions[j].split("\\\\")[0])) {
							charactersSoFar.add(conditions[j].split("\\\\")[0]);
							charactersDefinition = charactersDefinition + conditions[j].split("\\\\")[0] + "/1,"
							+ conditions[j].split("\\\\")[0] + "/0,";
						}
						if(!charactersSoFar.contains(conditions[j].split("\\\\")[1])) {
							charactersSoFar.add(conditions[j].split("\\\\")[1]);
							charactersDefinition = charactersDefinition + conditions[j].split("\\\\")[1] + "/1,"
							+ conditions[j].split("\\\\")[1] + "/0,";
						}
					}
					else if(!charactersSoFar.contains(conditions[j])) {
						charactersSoFar.add(conditions[j]);
						charactersDefinition = charactersDefinition + conditions[j] + "/1,"
						+ conditions[j] + "/0,";
					}
				}
				if(headPreConditions[1].split("pragma")[0].contains(",")) {
					if(headPreConditions[1].contains("|"))
						actions = headPreConditions[1].split("pragma")[0].split("\\|")[1].split(",");
					else
						actions = headPreConditions[1].split("pragma")[0].split(",");
					for(int j = 0; j < actions.length;j++) {
						if(!charactersSoFar.contains(actions[j]) && !actionsSoFar.contains(actions[j])) {
							//charactersSoFar.add(actions[j]);
							charactersDefinition = charactersDefinition + actions[j] + "/0,";
							actionsSoFar.add(actions[j]);
						}
					}
				}
				else if(!charactersSoFar.contains(headPreConditions[1].split("pragma")[0]) && !actionsSoFar.contains(headPreConditions[1].split("pragma")[0])) {
					charactersDefinition = charactersDefinition + headPreConditions[1].split("pragma")[0] + "/0,";
					actionsSoFar.add(headPreConditions[1].split("pragma")[0]);
				}
				
			}
			
			// 
			writer.println(charactersDefinition.substring(0,charactersDefinition.length() - 1) + ".");
			writer.println();
	//		System.out.println(charactersSoFar.get(2));
			GenerateIDRules(charactersSoFar, writer);
			writer.println();
			GenerateMatchRules(Rules, writer);
			writer.println();
			writer.println("start <=> conflictdone.");
			writer.println();
			writer.println();
			
			// Matching Part
			writer.println("history(L),conflictdone\\match(R,_,IDs) <=>");
			writer.println("member((R,FIDs),L),sort(IDs,II),sort(FIDs,II)|true.");
			writer.println();
			
			// Resolving Part
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
			GenerateFiringRules(Rules, writer);
			
			writer.close();
			
		}
		 
	}
	
	private void GenerateIDRules(ArrayList<String> charactersSoFar, PrintWriter writer) {
		for(int i = 0; i < charactersSoFar.size(); i++) {
			writer.println("id(I), " + charactersSoFar.get(i) + " <=> " + charactersSoFar.get(i) + "(I), I1 is I+1,id(I1).");
		}
	}
	
	private void GenerateMatchRules(ArrayList<String> Rules, PrintWriter writer) {
		for(int i = 0; i < Rules.size(); i++) {
			String ruleHead;
			String ruleGuards = "";
			String rulePriority = Rules.get(i).split("pragma")[1];
			if(Rules.get(i).contains("<=>")) {
				ruleHead = Rules.get(i).split("<=>")[0];
				if(Rules.get(i).split("<=>")[1].contains("|")) {
					ruleGuards = Rules.get(i).split("<=>")[1].split("\\|")[0];
					System.out.println(Rules.get(i).split("<=>")[1]);
					System.out.println(Rules.get(i).split("<=>")[1].split("\\|")[1]);
					System.out.println(ruleGuards);
				}
			}
			else {
				ruleHead = Rules.get(i).split("==>")[0];
				if(Rules.get(i).split("==>")[1].contains("|")) {
					ruleGuards = Rules.get(i).split("==>")[1].split("|")[0];
				}
			}	
			String[] conditions = ruleHead.split("[,\\\\]");
			String ruleToPrint = "r" +(i+1) + "@" + " ";
			ArrayList<String> ruleIDs = new ArrayList<String>();
			for(int j = 0; j < conditions.length;j++) {
//				if(conditions[j].contains("\\")) {
//					if(j == 0) {
//						ruleToPrint += "start, " + conditions[j].split("\\\\")[0] + "(ID) , " 
//					+ conditions[j].split("\\\\")[1] + "(ID1) ";
//						ruleIDs.add("ID");
//						ruleIDs.add("ID1");
//						
//					}
//					else {
//						ruleToPrint += ", " + conditions[j].split("\\\\")[0] + "(ID" + j + ") , "
//					+ conditions[j].split("\\\\")[1] +  "(ID" + (j+1) + ")";
//						ruleIDs.add("ID" + j);
//						ruleIDs.add("ID" + (j+1));
//						
//					}
//				}
				if(j == 0) {
					ruleToPrint += "start, " + conditions[j] + "(ID) ";
					ruleIDs.add("ID");
				}
				else {
					ruleToPrint += ", " + conditions[j] + "(ID" + j + ")";
					ruleIDs.add("ID" + j);
				}
			}
			
			ruleToPrint += " ==> ";
			String[] guardConditions;
			if(!ruleGuards.contains(",") && ruleGuards != "") {
				ruleToPrint += ruleGuards + " | ";
			}
			else if(ruleGuards != ""){
				guardConditions = ruleGuards.split(",");
				for(int j = 0;j < guardConditions.length;j++) {
					if(j > 0)
						ruleToPrint += ", ";
					ruleToPrint += guardConditions[j];
					if(j == guardConditions.length -1)
						ruleToPrint += "|";
					
				}		
			}
			ruleToPrint += " match(r" + (i+1) + "," + rulePriority + "," + ruleIDs + ").";
			writer.println(ruleToPrint);
		}
	}
	
	private void GenerateFiringRules(ArrayList<String> Rules, PrintWriter writer) {
		for(int i = 0; i < Rules.size(); i++) {
			String ruleToAdd = "";
			// Simpagation Rules
			ArrayList<String> ruleIDs = new ArrayList<String>();
			if(Rules.get(i).contains("<=>") && Rules.get(i).contains("\\")) {
				String conditionsToBeKept = Rules.get(i).split("\\\\")[0];
				String conditionsToBeRemoved = Rules.get(i).split("<=>")[0].split("\\\\")[1];
				String[] conditionsKeptArray = conditionsToBeKept.split(",");
				String[] conditionsRemovedArray = conditionsToBeRemoved.split(",");
				for(int j = 0; j < conditionsKeptArray.length; j++) {
					if(j == 0) {
						ruleToAdd += conditionsKeptArray[j] + "(ID)";
						ruleIDs.add("ID");
					} 
					else {
						ruleToAdd += conditionsKeptArray[j] + "(ID" + j + ")";
						ruleIDs.add("ID" + j);
					}
					if(j < conditionsKeptArray.length - 1)
						ruleToAdd += ",";
					
				}
				ruleToAdd += "\\";
				for(int j = 0; j < conditionsRemovedArray.length; j++) {
					ruleToAdd += conditionsRemovedArray[j] + "(ID" + ((conditionsKeptArray.length)+j) + ")" ;
					ruleToAdd += ",";
					ruleIDs.add("ID" + ((conditionsKeptArray.length)+j));
				}
				
			}
			// Probagation Rules
			else if(Rules.get(i).contains("<=>")) {
				String conditions = Rules.get(i).split("<=>")[0];
				String[] conditionsArray = conditions.split(",");
				for(int j = 0; j < conditionsArray.length; j++) {
					if(j == 0) {
						ruleToAdd += conditionsArray[j] + "(ID)";
						ruleIDs.add("ID");
					}
					else {
						ruleToAdd += conditionsArray[j] + "(ID" + j + ")";
						ruleIDs.add("ID" + j);
					}
						ruleToAdd += ",";
				}
			}
			// Simplification Rules
			else {
				String conditions = Rules.get(i).split("==>")[0];
				String[] conditionsArray = conditions.split(",");
				for(int j = 0; j < conditionsArray.length; j++) {
					if(j == 0) {
						ruleToAdd += conditionsArray[j] + "(ID)";
						ruleIDs.add("ID");
					}
					else {
						ruleToAdd += conditionsArray[j] + "(ID" + j + ")";
						ruleIDs.add("ID" + j);
					}
					if(j < conditionsArray.length - 1)
						ruleToAdd += ",";
				}
				ruleToAdd += "\\";
				
			}
			ruleToAdd += "history(L),fire,match(r" + (i+1) + ",_," + ruleIDs + ") <=> ";
			//TODO Adding actions and adding to history
			String actionsToAdd = "";
			if(Rules.get(i).contains("<=>"))
				actionsToAdd = Rules.get(i).split("<=>")[1].split("pragma")[0];
			else
				actionsToAdd = Rules.get(i).split("==>")[1].split("pragma")[0];
			if(!actionsToAdd.contains(",")) {
				ruleToAdd += actionsToAdd + ", ";
			}
			else {
				String[] actionsArray = actionsToAdd.split(",");
			
				for(int j = 0; j < actionsArray.length; j++) {
					ruleToAdd += actionsArray[j] + ", ";
				}
			}
			ruleToAdd += "history([(r" + (i+1) + "," + ruleIDs + ")|L]),start.";
			writer.println(ruleToAdd);
		}
	}
	
	
	
}