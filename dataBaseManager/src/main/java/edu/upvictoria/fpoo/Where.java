package edu.upvictoria.fpoo;

import java.util.ArrayList;
import java.util.HashMap;

public class Where {
	/**
	 * Steps to do it
	 * Im gonna store all the operators
	 * Then im gonna store all the conditionals
	 * Then im going to recursively evaluate all the conditionals
	 * as a stop case, when the index is greater than the operator array itself im
	 * going to return the boolean
	 */

	/**
	 * This method is going to manage the where clause
	 * @param condicionales A string of everything that comes before where clause
	 * @param line The line that is going to be evaluated
	 * @param ind A map of (name, index) of the columns
	 * @param type A map of (name, type) of the columns
	 * @return A boolean that is going to be true if the line is going to be added to the result
	 * @throws Exception If the where clause is invalid
	  */
	public static boolean manageWhere(String condicionales, String line, String tableName) throws Exception {
		if(condicionales.equals("")) return true;
		
		String headerOfTable = Utilities.getHeaderOfTable(tableName);
        ArrayList<TypeBuilder> types = FileManagement.decompressInfo(tableName);

        HashMap<String, Integer> indexMap = new HashMap<>();
        HashMap<String, String> typeMap = new HashMap<>();

        String[] headerOfTableBreak = headerOfTable.split(",");
        for (int i = 0; i < headerOfTableBreak.length; i++) {
            indexMap.put(headerOfTableBreak[i], i);

            for (int j = 0; j < types.size(); j++)
                if (types.get(j).getName().equals(headerOfTableBreak[i]))
                    typeMap.put(headerOfTableBreak[i], types.get(j).getDataType());
        }

		// As a parameter i need the conditional sentence, the line i will evaluate, and the map of (name,index) and a hm of types
		ArrayList<String> conditionals = new ArrayList<>();
		ArrayList<String> operators = new ArrayList<>();

		String[] condicionalesBreak = condicionales.split(" ");
		String conditional = "";

		for (int i = 0; i < condicionalesBreak.length; i++) {
			if(Utilities.isLogic(condicionalesBreak[i])){
				operators.add(condicionalesBreak[i]);
				conditionals.add(conditional);
				conditional = "";
			} else
				conditional+=condicionalesBreak[i];				
		}
		if(!conditional.equals("")) conditionals.add(conditional); 

		if(conditionals.size() - 1 != operators.size())
			throw new IllegalArgumentException("Sentencia WHERE inválida");

		return recursiveEvaluation(indexMap, typeMap, conditionals, operators, 0, line);
	}

	public static boolean recursiveEvaluation(HashMap<String,Integer> ind, HashMap<String, String> type, 
	ArrayList<String> conditionals, ArrayList<String> operators, int index, String line) throws Exception {
		String operation = conditionals.get(index);
		String operator = "";;

		if(operation.contains("<>")){
			operator = "!=";
			operation = operation.replace("<>", ",");
		} else if(operation.contains("<=")) {
			operator = "<=";
			operation = operation.replace("<=", ",");
		} else if(operation.contains(">=")) {
			operator = ">=";
			operation = operation.replace(">=", ",");
		} else if(operation.contains("<")) {
			operator = "<";
			operation = operation.replace("<", ",");
		} else if(operation.contains(">")) {
			operator = ">";
			operation = operation.replace(">", ",");
		} else if(operation.contains("!=")) {
			operator = "!=";
			operation = operation.replace("!=", ",");
		} else if(operation.contains("=")) {
			operator = "=";
			operation = operation.replace("=", ",");
		} 

		if(operator.equals("")) throw new IllegalArgumentException("Operador no encontrado en la sentencia WHERE");

		String[] operationBreak = operation.split(",");

		String name = operationBreak[0];
		String value = operationBreak[1];
		String typeValue = type.get(name);

		if(value.equals("null")) return false;
		if(typeValue == null) throw new IllegalArgumentException("Columna en el where no encontrada");

		boolean resultBoolean = false;
		String[] lineBreak = line.split(",");
		
		switch (typeValue) {
			case "int":
				switch (operator) {
					case "=":
						resultBoolean = Integer.parseInt(lineBreak[ind.get(name)]) == Integer.parseInt(value);
						break;
					case "!=":
						resultBoolean = Integer.parseInt(lineBreak[ind.get(name)]) != Integer.parseInt(value);
						break;
					case "<":
						resultBoolean = Integer.parseInt(lineBreak[ind.get(name)]) < Integer.parseInt(value);
						break;
					case ">":
						resultBoolean = Integer.parseInt(lineBreak[ind.get(name)]) > Integer.parseInt(value);
						break;
					case "<=":
						resultBoolean = Integer.parseInt(lineBreak[ind.get(name)]) <= Integer.parseInt(value);
						break;
					case ">=":
						resultBoolean = Integer.parseInt(lineBreak[ind.get(name)]) >= Integer.parseInt(value);
						break;
					default:
						break;
				}
			case "double":
				switch (operator) {
					case "=":
						resultBoolean = Math.round(Double.parseDouble(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 == Math.round(Double.parseDouble(value) * 10000000.0) / 10000000.0;
						break;
					case "!=":
						resultBoolean = Math.round(Double.parseDouble(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 != Math.round(Double.parseDouble(value) * 10000000.0) / 10000000.0;
						break;
					case "<":
						resultBoolean = Double.parseDouble(lineBreak[ind.get(name)]) < Double.parseDouble(value);
						break;
					case ">":
						resultBoolean = Double.parseDouble(lineBreak[ind.get(name)]) > Double.parseDouble(value);
						break;
					case "<=":
						resultBoolean = Math.round(Double.parseDouble(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 <= Math.round(Double.parseDouble(value) * 10000000.0) / 10000000.0;
						break;
					case ">=":
						resultBoolean = Math.round(Double.parseDouble(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 >= Math.round(Double.parseDouble(value) * 10000000.0) / 10000000.0;
						break;
					default:
						break;
				}
			case "float":
				switch (operator) {
					case "=":
						resultBoolean = Math.round(Float.parseFloat(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 == Math.round(Float.parseFloat(value) * 10000000.0) / 10000000.0;
						break;
					case "!=":
						resultBoolean = Math.round(Float.parseFloat(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 != Math.round(Float.parseFloat(value) * 10000000.0) / 10000000.0;
						break;
					case "<":
						resultBoolean = Math.round(Float.parseFloat(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 < Math.round(Float.parseFloat(value) * 10000000.0) / 10000000.0;
						break;
					case ">":
						resultBoolean = Math.round(Float.parseFloat(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 > Math.round(Float.parseFloat(value) * 10000000.0) / 10000000.0;
						break;
					case "<=":
						resultBoolean = Math.round(Float.parseFloat(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 <= Math.round(Float.parseFloat(value) * 10000000.0) / 10000000.0;
						break;
					case ">=":
						resultBoolean = Math.round(Float.parseFloat(lineBreak[ind.get(name)]) * 10000000.0) / 10000000.0 >= Math.round(Float.parseFloat(value) * 10000000.0) / 10000000.0;
						break;
					default:
						break;
				}
			case "varchar":
				switch (operator) {
					case "=":
						resultBoolean = lineBreak[ind.get(name)].equals(value);
						break;
					case "!=":
						resultBoolean = !lineBreak[ind.get(name)].equals(value);
						break;
					case "<=":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) <= 0;
						break;
					case ">=":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) >= 0;
						break;
					case "<":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) < 0;
						break;
					case ">":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) > 0;
						break;
					default:
						break;
				}
			default:
				break;
			case "date":
				switch (operator) {
					case "=":
						resultBoolean = lineBreak[ind.get(name)].equals(value);
						break;
					case "!=":
						resultBoolean = !lineBreak[ind.get(name)].equals(value);
						break;
					case "<=":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) <= 0;
						break;
					case ">=":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) >= 0;
						break;
					case "<":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) < 0;
						break;
					case ">":
						resultBoolean = lineBreak[ind.get(name)].compareTo(value) > 0;
						break;
					default:
						break;
				}
		}

		if(index < operators.size()) 
			if(operators.get(index).equalsIgnoreCase("AND")) 
				return resultBoolean && recursiveEvaluation(ind, type, conditionals, operators, ++index, line);
			else 
				return resultBoolean || recursiveEvaluation(ind, type, conditionals, operators, ++index, line);
		
		return resultBoolean;
	}
}
