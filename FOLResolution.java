import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class FOLResolution {
	static ArrayList<Term> Queries = new ArrayList<>();
	static ArrayList<Sentence> KB = new ArrayList<>();

	public static void main(String[] args) {
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(new File("input.txt"));
			br = new BufferedReader(fr);
			int numberOfQueries, numberOfSentences;
			numberOfQueries = Integer.parseInt(br.readLine().trim());
			boolean[] queryResults = new boolean[numberOfQueries];
			String inputTerm = "";
			int i, j;
			for (i = 0; i < numberOfQueries; i++) {
				inputTerm = br.readLine().trim();
				Term query = new Term(inputTerm);
				Queries.add(query);
			}
			numberOfSentences = Integer.parseInt(br.readLine().trim());
			String[] termsInInputLine;
			for (i = 0; i < numberOfSentences; i++) {
				termsInInputLine = br.readLine().trim().split("\\|");
				HashSet<Term> inputSentence = new HashSet<>();
				for (j = 0; j < termsInInputLine.length; j++) {
					Term term = new Term(termsInInputLine[j].trim());
					inputSentence.add(term);
				}
				Sentence tempSentence = new Sentence(inputSentence);
				KB.add(tempSentence);
			}
			standardize(KB, 1);
			BufferedWriter bw = null;
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt")));
			String resultString = "";
			for (i = 0; i < numberOfQueries; i++) {
				final long maxTime = System.currentTimeMillis() + 60*1000;
				queryResults[i] = checkQuery(Queries.get(i), maxTime);
				resultString = resultString + (queryResults[i] ? "TRUE" : "FALSE");
				resultString = resultString + "\n";
			}
			bw.write(resultString);
			fr.close();
			br.close();
			bw.close();
		} catch (Exception e) {
			System.out.println("Exception");
			e.printStackTrace();
		}
	}

	private static boolean checkQuery(Term term,final long maxTime) {
		int i, j;
		HashSet<Sentence> clauses = new HashSet<>();
		HashSet<Sentence> newSentencesSet = new HashSet<>();
		for (Sentence item : KB) {
			Sentence s = new Sentence();
			s.terms = (HashSet<Term>) item.terms.clone();
			clauses.add(item);
		}
		term.isNegated = !term.isNegated;
		HashSet<Term> tempSentenceTerm = new HashSet<>();
		tempSentenceTerm.add(term);
		newSentencesSet.add(new Sentence(tempSentenceTerm));
		ArrayList<Sentence> resultOfPrevRun = new ArrayList<>();
		resultOfPrevRun.add(new Sentence(tempSentenceTerm));
		clauses.add(new Sentence(tempSentenceTerm));
		while (true) {
			if(System.currentTimeMillis()>maxTime) {
				return false;
			}
			Sentence[] clausesArray = clauses.toArray(new Sentence[clauses.size()]);
			Sentence[] newSentencesArray = resultOfPrevRun.toArray(new Sentence[resultOfPrevRun.size()]);
			int prevSizeOfPrevRunList = 1;
			boolean addedSentence = false;
			for (i = 0; i < clausesArray.length; i++) {
				if (i == 0) {
					prevSizeOfPrevRunList = resultOfPrevRun.size();
				}
				ArrayList<Sentence> temp = new ArrayList<>();
				for (j = 0; j < newSentencesArray.length; j++) {
					temp.addAll(resolve(clausesArray[i], newSentencesArray[j]));
				}
				for (Sentence resolvedSentence : temp) {
					if (resolvedSentence.terms.isEmpty()) {
						return true;
					} else {
						if (!newSentencesSet.contains(resolvedSentence)) {
							addedSentence = true;
							newSentencesSet.add(resolvedSentence);
							resultOfPrevRun.add(resolvedSentence);
						} else {

						}
					}
				}
			}
			resultOfPrevRun = new ArrayList<Sentence>(
					resultOfPrevRun.subList(prevSizeOfPrevRunList, resultOfPrevRun.size()));
			if (!addedSentence) {
				return false;
			}
		}
	}

	public static ArrayList<Sentence> resolve(final Sentence sentenceFromClauses, final Sentence sentenceFromNew) {
		ArrayList<Sentence> result = new ArrayList<>();
		HashMap<String, String> subs;
		for (Term termFromClauses : sentenceFromClauses.terms) {
			for (Term termFromNew : sentenceFromNew.terms) {
				if (termFromClauses.predicate.equals(termFromNew.predicate)
						&& termFromClauses.isNegated != termFromNew.isNegated) {
					subs = new HashMap<>();
					subs = unify(termFromClauses, termFromNew, subs);
					if (subs != null && !subs.isEmpty()) {
						Sentence resolvedSentence = new Sentence();
						for (Term A_term : sentenceFromClauses.terms) {
							if (!A_term.equals(termFromClauses)) {
								Term termToAdd = new Term();
								termToAdd.isNegated = A_term.isNegated;
								termToAdd.predicate = A_term.predicate;
								for (String args : A_term.arguements) {
									if (subs.containsKey(args)) {
										termToAdd.arguements.add(subs.get(args));
									} else {
										termToAdd.arguements.add(args);
									}
								}
								resolvedSentence.terms.add(termToAdd);
							}
						}
						for (Term B_term : sentenceFromNew.terms) {
							if (!B_term.equals(termFromNew)) {
								Term termToAdd = new Term();
								termToAdd.isNegated = B_term.isNegated;
								termToAdd.predicate = B_term.predicate;
								for (String args : B_term.arguements) {
									if (subs.containsKey(args)) {
										termToAdd.arguements.add(subs.get(args));
									} else {
										termToAdd.arguements.add(args);
									}
								}
								resolvedSentence.terms.add(termToAdd);
							}
						}
						resolvedSentence = factorize(resolvedSentence);
						result.add(resolvedSentence);
						if (resolvedSentence.terms.isEmpty()) {
							return result;
						}
					} else {
						if (termFromClauses.arguements.equals(termFromNew.arguements)) {
							Sentence resolvedSentence = new Sentence();
							for (Term A_term : sentenceFromClauses.terms) {
								if (!A_term.equals(termFromClauses)) {
									Term termToAdd = new Term();
									termToAdd.isNegated = A_term.isNegated;
									termToAdd.predicate = A_term.predicate;
									termToAdd.arguements = (ArrayList<String>) A_term.arguements.clone();
									resolvedSentence.terms.add(termToAdd);
								}
							}
							for (Term B_term : sentenceFromNew.terms) {
								if (!B_term.equals(termFromNew)) {
									Term termToAdd = new Term();
									termToAdd.isNegated = B_term.isNegated;
									termToAdd.predicate = B_term.predicate;
									termToAdd.arguements = (ArrayList<String>) B_term.arguements.clone();
									resolvedSentence.terms.add(termToAdd);
								}
							}
							resolvedSentence = factorize(resolvedSentence);
							result.add(resolvedSentence);
							if (resolvedSentence.terms.isEmpty()) {
								return result;
							}
						}
					}
				}
			}
		}
		return result;
	}

	public static boolean allVars(Term t) {
		for (String s : t.arguements) {
			if (!isVar(s)) {
				return false;
			}
		}
		return true;
	}

	public static Sentence factorize(Sentence s) {
		Term[] termsArray = s.terms.toArray(new Term[s.terms.size()]);
		int i, j;
		Sentence result = new Sentence();
		HashSet<Term> resultTerms = new HashSet();
		resultTerms.addAll(s.terms);
		for (i = 0; i < termsArray.length; i++) {
			for (j = 0; j < termsArray.length; j++) {
				if (i == j) {
					continue;
				}
				if (termsArray[i].predicate.equals(termsArray[j].predicate)
						&& termsArray[i].isNegated == termsArray[j].isNegated) {
					if (allVars(termsArray[i]) && resultTerms.size()>2) {
						resultTerms.remove(termsArray[i]);
					} else if (allVars(termsArray[j]) && resultTerms.size()>2) {
						resultTerms.remove(termsArray[j]);
					}
				}
			}
		}
		result.terms = resultTerms;
		return result;
	}

	public static void standardize(ArrayList<Sentence> KB, int i) {
		for (Sentence sentence : KB) {
			for (Term term : sentence.terms) {
				ArrayList<String> newArgs = new ArrayList<>();
				for (String arg : term.arguements) {
					if (arg.charAt(0) >= 'a' && arg.charAt(0) <= 'z') {
						arg = "" + arg.charAt(0) + i;
						newArgs.add(arg);
					} else {
						newArgs.add(arg);
					}
				}
				term.arguements = newArgs;
			}
			i++;
		}
	}

	public static HashMap<String, String> unify(Object A, Object B, HashMap<String, String> hm) {
		if (hm == null) {
			return null;
		} else if (A.equals(B)) {
			return hm;
		} else if (isVar(A)) {
			return unifyVar((String) A, (String) B, hm);
		} else if (isVar(B)) {
			return unifyVar((String) B, (String) A, hm);
		} else if (A instanceof Term && B instanceof Term) {
			Term term1 = (Term) A;
			Term term2 = (Term) B;
			if (term1.predicate.equals(term2.predicate) && term1.arguements.equals(term2.arguements)) {
				return hm;
			}
			return unify(term1.arguements, term2.arguements, hm);
		} else if (A instanceof ArrayList<?> && B instanceof ArrayList<?>) {
			ArrayList<String> args1 = (ArrayList<String>) A;
			ArrayList<String> args2 = (ArrayList<String>) B;
			if (args1.size() != args2.size()) {
				return null;
			}
			String argTerm1 = args1.size() > 0 ? args1.get(0) : null;
			String argTerm2 = args2.size() > 0 ? args2.get(0) : null;
			ArrayList<String> copyArgs1 = (ArrayList<String>) args1.clone();
			ArrayList<String> copyArgs2 = (ArrayList<String>) args2.clone();
			if (copyArgs1.size() > 0 && copyArgs2.size() > 0) {
				copyArgs1.remove(0);
				copyArgs2.remove(0);
			}
			return unify(copyArgs1, copyArgs2, unify(argTerm1, argTerm2, hm));
		} else {
			return null;
		}
	}

	public static HashMap<String, String> unifyVar(String var, String x, HashMap<String, String> hm) {
		if (hm.containsKey(var)) {
			return unify(hm.get(var), x, hm);
		} else if (hm.containsKey(x)) {
			return unify(var, hm.get(x), hm);
		} else {
			hm.put(var, x);
			return hm;
		}
	}

	public static boolean isVar(Object x) {
		if (!(x instanceof String)) {
			return false;
		}
		String input = (String) x;
		if (input.charAt(0) >= 'a' && input.charAt(0) <= 'z') {
			return true;
		}
		return false;
	}
	
}
