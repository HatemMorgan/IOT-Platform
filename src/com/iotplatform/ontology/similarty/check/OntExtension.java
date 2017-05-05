package com.iotplatform.ontology.similarty.check;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.wordnet.GraphLoader_Wordnet;
import slib.graph.io.loader.wordnet.GraphLoader_Wordnet_Full;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.indexer.wordnet.IndexerWordNetBasic;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.file.Morphology;

//@Component
public class OntExtension {
	public boolean isClass;
	public boolean isProperty;
	public boolean isVerb;
	public ArrayList<String> nounOfGiven;
	public ArrayList<String> nounOfExisting;
	public ArrayList<String> verbOfGiven;
	public ArrayList<String> verbOfExisting;
	public ArrayList<String> adjOfGiven;
	public ArrayList<String> adjOfExisting;
	public ArrayList<String> advOfGiven;
	public ArrayList<String> advOfExisting;
	public String[] sentenceOfGiven;
	public String[] sentenceOfExisting;
	public ArrayList<String> miscOfGiven;
	public ArrayList<String> miscOfExsisting;
	public ArrayList<String> forArray;
	public ArrayList<String> byArray;
	public String[] classArr;
	public String[] propArr;
	//
	// public OntExtension(String[] classArr, String[] propArr) {
	// this.classArr = classArr;
	// this.propArr = propArr;
	// }

	// name of class will be entered like end_time or one word
	public String checkClassSimilarity(String nameofClass, Object[] classArr) throws SLIB_Exception, IOException {

		// OntModel ont = ModelFactory.createOntologyModel(
		// OntModelSpec.OWL_MEM, null);
		// // OntDocumentManager dm = foafOnt.getDocumentManager();
		// //if there is an internet problem load ontology from file specified
		// //dm.addAltEntry("http://xmlns.com/foaf/0.1/",
		// "file:"+"E:/Lectures/Bachelor/Jena-App/Jena/src/foaf.rdf");
		// //read from file
		// //ont.read("E:/Lectures/Bachelor/Jena-App/Jena/src/foaf.rdf");
		// ont.read(ontURI);
		// check ontUri
		// OntClass sensor=foafOnt.getOntClass(ontURI+"Sensor");

		// String [] classComments=new String []();
		double max = 0.0;
		boolean similar = false;
		String name = " ";
		for (int i = 0; i < classArr.length; i++) {
			String ontclass = classArr[i].toString();
			// }
			// for (Iterator<OntClass> i = ont.listClasses(); i.hasNext();) {
			// String ontclass=i.next().getLocalName();
			if (formatName(ontclass).equalsIgnoreCase(formatName(nameofClass))) {
				System.out.println("CLASS ALREADY EXISTS:Please enter another name of class:");
				return "true" + "!" + ontclass + "!" + 1.0;
			}

			else {
				String[] x = divideName(nameofClass);
				String[] y = divideName(ontclass);
				// check if class
				assignArrayGivenClass(x);
				assignArrayExsistingClass(y);
				double[] scores = compareNormal(nounOfGiven, verbOfGiven, nounOfExisting, verbOfExisting);
				double[] scores1 = getRelationsNormal(nameofClass.toLowerCase(), ontclass.toLowerCase(), nounOfGiven,
						verbOfGiven, miscOfGiven, nounOfExisting, verbOfExisting, miscOfExsisting);

				int countNaN = 4;
				if (Double.isNaN(scores[0])) {
					System.out.println("www" + Double.isNaN(scores[0]));
					scores[0] = 0.0;
					countNaN--;
				}
				if (Double.isNaN(scores[1])) {
					System.out.println("www" + Double.isNaN(scores[1]));
					scores[1] = 0.0;
					countNaN--;
				}
				if (Double.isNaN(scores[2])) {
					System.out.println("www" + Double.isNaN(scores[2]));
					scores[2] = 0.0;
					countNaN--;
				}
				if (Double.isNaN(scores[3])) {
					System.out.println("www" + Double.isNaN(scores[3]));
					scores[3] = 0.0;
					countNaN--;
				}
				System.out.println(countNaN);
				double finalScore = (scores[0] + scores[1] + scores[2] + scores[3]) / countNaN;
				int countNaN1 = 6;
				if (Double.isNaN(scores1[0])) {
					System.out.println("www" + Double.isNaN(scores1[0]));
					scores1[0] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[1])) {
					System.out.println("www" + Double.isNaN(scores1[1]));
					scores1[1] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[2])) {
					System.out.println("www" + Double.isNaN(scores1[2]));
					scores1[2] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[3])) {
					System.out.println("www" + Double.isNaN(scores1[3]));
					scores1[3] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[4])) {
					System.out.println("www" + Double.isNaN(scores1[4]));
					scores1[4] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[5])) {
					System.out.println("www" + Double.isNaN(scores1[5]));
					scores1[5] = 0.0;
					countNaN1--;
				}
				if (scores1[6] == 0.0) {
					// System.out.println("www"+Double.isNaN(scores1[5]));
					// scores1[5]=0.0;
					countNaN1--;
				}
				double finalScore1 = (scores1[0] + scores1[1] + scores1[2] + scores1[3] + scores1[4] + scores1[5]
						+ scores1[6]) / countNaN1;

				System.err.println(scores[0] + " " + scores[1] + " " + scores[2] + " " + scores[3]);
				System.err.println(finalScore);
				System.err.println("hi: " + scores1[0] + " " + scores1[1] + " " + scores1[2] + " " + scores1[3] + " "
						+ scores1[4] + " " + scores1[5] + " " + scores1[6]);
				System.err.println("hi: " + finalScore1);

				double finalFinal = 0.0;
				if (finalScore1 != 0) {
					finalFinal = (finalScore + finalScore1) / 2;

				} else {
					finalFinal = finalScore;
				}

				if (max <= finalFinal) {
					max = finalFinal;
					name = ontclass;
				}

			}

		}
		String result = " ";
		if (max >= 0.50090) {
			similar = true;
			result = similar + "!" + name + "!" + max;

		} else {
			similar = false;
			result = similar + "!" + name + "!" + max;
		}

		return result;
	}

	// check all properties or not
	public String checkPropertySimilarity(String nameofProp, Object[] propArr) throws SLIB_Exception, IOException {

		double max = 0.0;
		boolean similar = false;
		String name = " ";
		for (int i = 0; i < propArr.length; i++) {
			String ontprop = propArr[i].toString();
			// for (Iterator<OntProperty> i = ont.listAllOntProperties();
			// i.hasNext();) {
			// //check string
			// String ontprop=i.next().getLocalName();
			if (formatName(ontprop).equalsIgnoreCase(formatName(nameofProp))) {
				System.out.println("PROPERTY ALREADY EXISTS:Please enter another name of property:");
				return "true" + "!" + ontprop + "!" + 1.0;
			}

			else {
				// String[] x=divideName(nameofProp);
				// //check string
				// String[]y=divideName(ontprop);
				// //check if class
				// assignArrayGivenProperty(x);
				// assignArrayExsistingProperty(y);
				String hasFor = forBy(nameofProp, propArr);
				// boolean f=false;
				// if(f){
				if ((!hasFor.equals("no for")) && (!hasFor.equals(" "))) {
					return hasFor;

				} else {

					String[] x = divideName(nameofProp);
					// check string
					String[] y = divideName(ontprop);
					// check if class
					assignArrayGivenProperty(x);
					assignArrayExsistingProperty(y);
					int count = 0;
					double sum = 0.0;
					double[] scores = compareNormal(nounOfGiven, verbOfGiven, nounOfExisting, verbOfExisting);
					double[] scores1 = getRelationsNormal(nameofProp, ontprop, nounOfGiven, verbOfGiven, miscOfGiven,
							nounOfExisting, verbOfExisting, miscOfExsisting);

					// if a score is zero should i divide by four or three?
					int countNaN = 4;
					if (Double.isNaN(scores[0])) {
						System.out.println("www" + Double.isNaN(scores[0]));
						scores[0] = 0.0;
						countNaN--;
					}
					if (Double.isNaN(scores[1])) {
						System.out.println("www" + Double.isNaN(scores[1]));
						scores[1] = 0.0;
						countNaN--;
					}
					if (Double.isNaN(scores[2])) {
						System.out.println("www" + Double.isNaN(scores[2]));
						scores[2] = 0.0;
						countNaN--;
					}
					if (Double.isNaN(scores[3])) {
						System.out.println("www" + Double.isNaN(scores[3]));
						scores[3] = 0.0;
						countNaN--;
					}
					System.out.println(countNaN);
					double finalScore = (scores[0] + scores[1] + scores[2] + scores[3]) / countNaN;
					int countNaN1 = 7;
					if (Double.isNaN(scores1[0])) {
						System.out.println("www" + Double.isNaN(scores1[0]));
						scores1[0] = 0.0;
						countNaN1--;
					}
					if (Double.isNaN(scores1[1])) {
						System.out.println("www" + Double.isNaN(scores1[1]));
						scores1[1] = 0.0;
						countNaN1--;
					}
					if (Double.isNaN(scores1[2])) {
						System.out.println("www" + Double.isNaN(scores1[2]));
						scores1[2] = 0.0;
						countNaN1--;
					}
					if (Double.isNaN(scores1[3])) {
						System.out.println("www" + Double.isNaN(scores1[3]));
						scores1[3] = 0.0;
						countNaN1--;
					}
					if (Double.isNaN(scores1[4])) {
						System.out.println("www" + Double.isNaN(scores1[4]));
						scores1[4] = 0.0;
						countNaN1--;
					}
					if (Double.isNaN(scores1[5])) {
						System.out.println("www" + Double.isNaN(scores1[5]));
						scores1[5] = 0.0;
						countNaN1--;
					}
					if (scores1[6] == 0.0) {
						// System.out.println("www"+Double.isNaN(scores1[5]));
						// scores1[5]=0.0;
						countNaN1--;
					}
					double finalScore1 = (scores1[0] + scores1[1] + scores1[2] + scores1[3] + scores1[4] + scores1[5]
							+ scores1[6]) / countNaN1;

					System.err.println(scores[0] + " " + scores[1] + " " + scores[2] + " " + scores[3]);
					System.err.println(finalScore);
					System.err.println("hi: " + scores1[0] + " " + scores1[1] + " " + scores1[2] + " " + scores1[3]
							+ " " + scores1[4] + " " + scores1[5]);
					System.err.println("hi: " + finalScore1);

					double finalFinal = 0.0;
					if (finalScore1 != 0) {
						finalFinal = (finalScore + finalScore1) / 2;

					} else {
						finalFinal = finalScore;
					}

					if (max <= finalFinal) {
						max = finalFinal;
						name = ontprop;
					}

				}

			}

		}

		String result = "";
		if (max >= 0.50000) {
			similar = true;
			result = similar + "!" + name + "!" + max;

		} else {
			similar = false;
			result = similar + "!" + name + "!" + max;
		}
		return result;
	}

	public String checkProperty(String nameofProp, String domain, String range, Object[] classArr)
			throws SLIB_Exception, IOException {

		boolean rangeExists = false;
		boolean domainExists = false;
		for (int i = 0; i < classArr.length; i++) {
			// for (Iterator<OntClass> i = ont.listClasses(); i.hasNext();) {
			if (classArr[i].toString().equalsIgnoreCase(domain)) {
				domainExists = true;
			}
			if (classArr[i].toString().equalsIgnoreCase(range)) {
				rangeExists = true;
			}
		}

		if (rangeExists && domainExists) {
			return "correct";
		}
		if ((!rangeExists) && (!domainExists)) {
			return "both wrong";
		} else {
			if (!rangeExists) {
				return "range wrong";
			} else {
				if (!domainExists) {
					return "domain wrong";
				} else {
					return "correct";
				}
			}
		}

	}

	public String checkPropertyMain(String nameofProp, String domain, String range, Object[] propArr, Object[] classArr)
			throws SLIB_Exception, IOException {
		String checker = checkProperty(nameofProp, domain, range, classArr);
		String result = "";
		if (checker.equals("correct")) {
			result = checkPropertySimilarity(nameofProp, propArr);
			return result;
		} else {
			return checker;
		}
	}

	public String checkClassPropertyMain(String nameofClass, String nameofProp, String domain, String range,
			Object[] classArr, Object[] propArr) throws SLIB_Exception, IOException {
		// String checker=checkPropertyMain(nameofProp,domain,range);
		String checkClass = checkClassSimilarity(nameofClass, classArr);
		String[] checkClassArr = checkClass.split("!");

		if (checkClassArr[0].equalsIgnoreCase("false")) {
			String checkProp = checkPropertySimilarity(nameofProp, propArr);
			String[] checkPropArr = checkProp.split("!");
			if (checkPropArr[0].equalsIgnoreCase("false")) {
				// both are good
				// return "ok";
				return checkProp;
			} else {
				// added class but not property
				return "wrongProperty";
			}
		} else {
			// class not added
			return "wrongClass";
		}

	}

	public void isVerb(String word) throws MalformedURLException {
		String path = "dict/";
		URL url = new URL("file", null, path);

		// construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);

		dict.open();

		// look up first sense of the word "dog"
		IIndexWord idxWord = dict.getIndexWord(word, POS.VERB);
		// Iterator<IIndexWord>
		// i=dict.getIndexWordIterator(POS.ADJECTIVE);//loop and compare the get
		// def
		if (!(idxWord == null)) {
			dict.close();
			isVerb = true;
		} else
			dict.close();
		isVerb = false;

		/*
		 * File outputDirectory = new File(
		 * "E:/Lectures/Bachelor/parsedEnwiktionary.xml"); IWiktionaryEdition
		 * wkt = JWKTL.openEdition(outputDirectory); WiktionaryEntryFilter
		 * filter = new WiktionaryEntryFilter();
		 * filter.setAllowedWordLanguages(Language.ENGLISH);
		 * List<IWiktionaryEntry> entries = wkt.getEntriesForWord(word, filter);
		 * for (IWiktionaryEntry entry : entries) {
		 * 
		 * //
		 * System.out.println(WiktionaryFormatter.instance().formatHeader(entry)
		 * ); // System.out.println(entry.getWordEtymology().getText()); //
		 * System.out.println(entry.getGlosses().); if
		 * (entry.getPartOfSpeech().compareTo(PartOfSpeech.VERB) == 0) { isVerb
		 * = true; break;
		 * 
		 * } } // Close the Wiktionary edition. wkt.close();
		 * 
		 */ }

	public String formatName(String name) {
		/*
		 * String[] y = name.split("_"); for (int i = 0; i < y.length; i++) {
		 * y[i]=y[i].toUpperCase().toLowerCase(); System.out.println(y[i]); }
		 * String result=StringUtils.join(y, ' ');;
		 */// String z="thisIsMyString";
		String vs = "";
		if (name.equals("virtualsensor")) {
			vs = "VirtualSensor";
			String[] y = vs.split("(?=\\p{Lu})");
			String ontClass = StringUtils.join(y, '_');
			String result = ontClass.toUpperCase().toLowerCase().replace('_', ' ');

			// for formation of incoming string
			/*
			 * String c=result.charAt(0)+""; String k=c.toUpperCase();
			 * 
			 * System.out.println(k.charAt(0)); String
			 * j=result.replace(result.charAt(0), k.charAt(0));
			 * System.out.println(j+"  jjj");
			 */

			return result;
		} else {
			String nu = "";
			if (name.equals("normaluser")) {
				nu = "NormalUser";
				String[] y = nu.split("(?=\\p{Lu})");
				String ontClass = StringUtils.join(y, '_');
				String result = ontClass.toUpperCase().toLowerCase().replace('_', ' ');

				// for formation of incoming string
				/*
				 * String c=result.charAt(0)+""; String k=c.toUpperCase();
				 * 
				 * System.out.println(k.charAt(0)); String
				 * j=result.replace(result.charAt(0), k.charAt(0));
				 * System.out.println(j+"  jjj");
				 */

				return result;
			} else {
				String ti = "";
				if (name.equals("topic_interest")) {
					ti = "topicOfInterest";
					String[] y = ti.split("(?=\\p{Lu})");
					String ontClass = StringUtils.join(y, '_');
					String result = ontClass.toUpperCase().toLowerCase().replace('_', ' ');

					return result;

				} else {
					String[] y = name.split("(?=\\p{Lu})");
					String ontClass = StringUtils.join(y, '_');
					String result = ontClass.toUpperCase().toLowerCase().replace('_', ' ');

					return result;
				}
			}
		}
	}

	public String[] divideName(String name) {
		// iot doesn't exist
		if (name.equals("virtualsensor")) {
			name = "VirtualSensor";
		}
		if (name.equals("normaluser")) {
			name = "NormalUser";
		}
		if (name.equals("topic_interest")) {
			name = "topicOfInterest";
		}
		if (name.equals("googleMapsUrl")) {
			name = "googleMapUrl";
		}
		if (name.equals("mbox")) {
			name = "mailbox";
		}
		// if(name.equals("hasSubSystem")){
		// name="hasSubsystem";
		// }
		String iot = "IOT";
		String withoutIOT = "";
		if (name.contains(iot)) {
			withoutIOT = name.replaceAll("IOT", "");
			String[] y = withoutIOT.split("(?=\\p{Lu})");
			String ontClass = StringUtils.join(y, '_');
			// System.out.println(ontClass.toUpperCase().toLowerCase());
			String result = ontClass.toUpperCase().toLowerCase();

			String[] x = result.split("_");
			return x;
		} else {
			String n = "";

			if (name.contains("admin")) {
				n = name.replace("admin", "administrator");
				System.out.println(n);
				String[] y = n.split("(?=\\p{Lu})");
				String ontClass = StringUtils.join(y, '_');
				// System.out.println(ontClass.toUpperCase().toLowerCase());
				String result = ontClass.toUpperCase().toLowerCase();

				String[] x = result.split("_");
				return x;
			} else {
				if (name.contains("Admin")) {
					n = name.replace("Admin", "Administrator");
					System.out.println(n);
					String[] y = n.split("(?=\\p{Lu})");
					String ontClass = StringUtils.join(y, '_');
					// System.out.println(ontClass.toUpperCase().toLowerCase());
					String result = ontClass.toUpperCase().toLowerCase();

					String[] x = result.split("_");

					return x;
				}

				else {

					String[] y = name.split("(?=\\p{Lu})");
					String ontClass = StringUtils.join(y, '_');
					// System.out.println(ontClass.toUpperCase().toLowerCase());
					String result = ontClass.toUpperCase().toLowerCase();

					String[] x = result.split("_");

					return x;

				}

			}

		}
	}

	public void forByMethod(Object[] propArr) throws IOException {

		forArray = new ArrayList<String>();
		byArray = new ArrayList<String>();
		for (int i = 0; i < propArr.length; i++) {
			String x = propArr[i].toString();
			String[] com = divideName(propArr[i].toString());
			// for (Iterator<OntProperty> i = ssnOnt.listAllOntProperties() ;
			// i.hasNext(); ) {
			// String[] com= divideName(i.next().getLocalName());
			// // System.out.println(i.next().getComment(null));

			// if(x.toLowerCase().contains("for")){
			// if(!(com[0].equals("for"))){
			// // String
			// }
			// }

			if (!(com[0].equals("for"))) {
				if (!(com[0].equals("by"))) {
					for (int j = 1; j < com.length; j++) {

						if (!(getBaseForm(com[j - 1]).equals("not_verb"))) {
							if (com[j].equals("for")) {
								forArray.add(propArr[i].toString());

							}
							// else{
							// System.out.println("no for");
							// if(com[j+1].equals("by")){
							// byArray.add(x);
							// }
							// }
						}
					}
				}
			}
		}

		// System.out.println(forArray.isEmpty());

	}

	public String forBy(String name, Object[] propArr) throws IOException, SLIB_Ex_Critic, SLIB_Exception {
		// get all properties with for or by
		forByMethod(propArr);
		int score = 0;
		boolean hasFor = false;
		String[] nameSplit = divideName(name);
		if (!(nameSplit[0].equals("for"))) {
			if (!(nameSplit[0].equals("by"))) {
				for (int j = 1; j < nameSplit.length; j++) {
					if (!(getBaseForm(nameSplit[j - 1]).equals("not_verb"))) {
						if (nameSplit[j].equals("for")) {
							// forArray.add(i.next().getLocalName());
							// check consistency with all for conatinig
							// properties then add that to average

							hasFor = true;

						}

						//
						// else{
						// if(nameSplit[j+1].equals("by")){
						// //byArray.add(i.next().getLocalName());
						// }
						// }
					}
				}
			}

		}

		String result = " ";
		if (hasFor) {
			// check consistency with all for conatinig properties then add that
			// to average
			double max = 0.0;
			boolean similar = false;
			String name1 = " ";
			// System.out.println(forArray.size());
			for (int i = 0; i < forArray.size(); i++) {
				String[] propSplit = divideName(forArray.get(i));

				assignArrayGivenProperty(nameSplit);
				assignArrayExsistingProperty(propSplit);
				// int count=0;
				// double sum=0.0;
				// for (int j = 0; j < nounOfGiven.size(); j++) {
				// for (int k = 0; k < nounOfExisting.size(); k++) {
				// //compute sim
				// sum+=compareNouns(nounOfGiven.get(j), nounOfExisting.get(k));
				// count++;
				// }
				// }
				// double scoreNoun=sum/count;
				// sum=0.0;
				// count=0;
				// for (int j = 0; j < verbOfGiven.size(); j++) {
				// for (int k = 0; k < verbOfExisting.size(); k++) {
				// //compute sim
				// sum+=compareNouns(verbOfGiven.get(j), verbOfExisting.get(k));
				// count++;
				// }
				// }
				//
				// double scoreVerb=sum/count;
				// sum=0.0;
				// count=0;
				// for (int j = 0; j < nounOfGiven.size(); j++) {
				// for (int k = 0; k < verbOfExisting.size(); k++) {
				// //compute sim
				// sum+=compareNounsVerbs(nounOfGiven.get(j),
				// verbOfExisting.get(k));
				// count++;
				// }
				// }
				// double scoreNounVerb=sum/count;
				// sum=0.0;
				// count=0;
				// for (int j = 0; j < verbOfGiven.size(); j++) {
				// for (int k = 0; k < nounOfExisting.size(); k++) {
				// //compute sim
				// sum+=compareNounsVerbs(nounOfExisting.get(k),verbOfGiven.get(j));
				// count++;
				// }
				// }
				// double scoreVerbNoun=sum/count;
				// sum=0.0;
				// double sum2=0.0;
				// count=0;
				// for (int j = 0; j < miscOfGiven.size(); j++) {
				// for (int k = 0; k < miscOfExsisting.size(); k++) {
				// //compute sim
				// sum+=getRelationsSyn(miscOfGiven.get(j),
				// miscOfExsisting.get(k));
				// sum2+=getRelationsAnt(miscOfGiven.get(j),
				// miscOfExsisting.get(k));
				// count++;
				// }
				// }
				// //divide by count or not
				// double scoreMiscSyn=sum/count;
				// double scoreMiscAnt=sum2/count;
				//
				// //get general avg then compare
				// sum=0.0;
				// sum2=0.0;
				// count=0;
				// for (int j = 0; j < nounOfGiven.size(); j++) {
				// for (int k = 0; k < nounOfExisting.size(); k++) {
				// //compute sim
				// sum+=getRelationsSyn(nounOfGiven.get(j),
				// nounOfExisting.get(k));
				// sum2+=getRelationsAnt(nounOfGiven.get(j),
				// nounOfExisting.get(k));
				// count++;
				// }
				// }
				// double scoreNounSyn=sum/count;
				// double scoreNounAnt=sum2/count;
				// sum=0.0;
				// sum2=0.0;
				// count=0;
				// for (int j = 0; j < verbOfGiven.size(); j++) {
				// for (int k = 0; k < verbOfExisting.size(); k++) {
				// //compute sim
				// sum+=getRelationsSyn(verbOfGiven.get(j),
				// verbOfExisting.get(k));
				// sum2+=getRelationsAnt(verbOfGiven.get(j),
				// verbOfExisting.get(k));
				// count++;
				// }
				// }
				// double scoreVerbSyn=sum/count;
				// double scoreVerbAnt=sum2/count;
				// double
				// finalScore=(scoreMiscAnt+scoreMiscSyn+scoreNoun+scoreNounAnt+scoreNounSyn+scoreNounVerb+scoreVerb+scoreVerbAnt+scoreVerbNoun+scoreVerbSyn)/10;
				double[] scores = compareNormal(nounOfGiven, verbOfGiven, nounOfExisting, verbOfExisting);

				double[] scores1 = getRelationsNormal(name, forArray.get(i), nounOfGiven, verbOfGiven, miscOfGiven,
						nounOfExisting, verbOfExisting, miscOfExsisting);

				int countNaN = 4;
				if (Double.isNaN(scores[0])) {
					System.out.println("www" + Double.isNaN(scores[0]));
					scores[0] = 0.0;
					countNaN--;
				}
				if (Double.isNaN(scores[1])) {
					System.out.println("www" + Double.isNaN(scores[1]));
					scores[1] = 0.0;
					countNaN--;
				}
				if (Double.isNaN(scores[2])) {
					System.out.println("www" + Double.isNaN(scores[2]));
					scores[2] = 0.0;
					countNaN--;
				}
				if (Double.isNaN(scores[3])) {
					System.out.println("www" + Double.isNaN(scores[3]));
					scores[3] = 0.0;
					countNaN--;
				}
				System.out.println(countNaN);
				int countNaN1 = 6;
				if (Double.isNaN(scores1[0])) {
					System.out.println("www" + Double.isNaN(scores1[0]));
					scores1[0] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[1])) {
					System.out.println("www" + Double.isNaN(scores1[1]));
					scores1[1] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[2])) {
					System.out.println("www" + Double.isNaN(scores1[2]));
					scores1[2] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[3])) {
					System.out.println("www" + Double.isNaN(scores1[3]));
					scores1[3] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[4])) {
					System.out.println("www" + Double.isNaN(scores1[4]));
					scores1[4] = 0.0;
					countNaN1--;
				}
				if (Double.isNaN(scores1[5])) {
					System.out.println("www" + Double.isNaN(scores1[5]));
					scores1[5] = 0.0;
					countNaN1--;
				}
				if (scores1[6] == 0.0) {
					// System.out.println("www"+Double.isNaN(scores1[5]));
					// scores1[5]=0.0;
					countNaN1--;
				}
				double finalScore = (scores[0] + scores[1] + scores[2] + scores[3]) / countNaN;
				double finalScore1 = (scores1[0] + scores1[1] + scores1[2] + scores1[3] + scores1[4] + scores1[5]
						+ scores1[6]) / countNaN1;

				System.err.println(scores[0] + " " + scores[1] + " " + scores[2] + " " + scores[3]);
				System.err.println(finalScore);
				System.err.println("hi: " + scores1[0] + " " + scores1[1] + " " + scores1[2] + " " + scores1[3] + " "
						+ scores1[4] + " " + scores1[5]);
				System.err.println("hi: " + finalScore1);

				double finalFinal = 0.0;
				if (finalScore1 != 0) {
					finalFinal = (finalScore + finalScore1) / 2;

				} else {
					finalFinal = finalScore;
				}
				if (max <= finalFinal) {
					max = finalFinal;
					name1 = forArray.get(i);
				}

			}

			if (max >= 0.500000) {
				similar = true;
				result = similar + "!" + name1 + "!" + max;

			} else {
				similar = false;
				result = similar + "!" + name1 + "!" + max;

			}

		} else {
			return "no for";
		}
		return result;
	}

	public static void main(String[] args) throws SLIB_Exception, IOException {
		// TODO Auto-generated method stub
		// string entered in this form
		// Object[] propArr = { "hasOutput" };
		// Object[] classArr = { "MaintenanceSchedule", "Accuracy", "Precision",
		// "Latency", "Process" };

		Object[] propArr = { "isMobile", "id", "metadataType", "metadataValue", "hasValue", "hasOperatingProperty",
				"inCondition", "hasMetadata", "hasOperatingRange", "hasCoverage", "exposedBy", "rangeOfTransmission",
				"hasType", "hasSubSystem", "hasBandwidth", "dutyCycle", "hasNetworkTopology", "hasSurvivalRange",
				"hasTransmissionPower", "hasFrequency", "description", "hasSurvivalProperty", "interfaceDescription",
				"interfaceType", "endpoint", "hasUnit", "hasRangeMinValue", "hasRangeMaxValue", "hasDataValue",
				"hasDevice", "onPlatform", "location", "hasOutput", "hasInput", "radius", "mbox", "loves", "middleName",
				"usesApplication", "familyName", "hates", "knows", "title", "userName", "gender", "age", "firstName",
				"birthday", "topic_interest", "isProducedBy", "name", "member", "hasQuantityKind", "implements",
				"observes", "hasMeasurementCapability", "detects", "hasSensingDevice", "adminOf",
				"observationResultTime", "observationSamplingTime", "qualityOfObservation", "observationResult",
				"observedProperty", "sensingMethodUsed", "featureOfInterest", "includesEvent", "hasProperty",
				"hasDeviceModule", "hasDeployment", "googleMapsUrl", "lat", "long", "hasMeasurementProperty",
				"forProperty", "usesSystem", "fundedBy", "isAssociatedWith", "hasAttribute", "isProxyFor",
				"developedApplication", "deployedOnPlatform", "deploymentProcessPart" };

		Object[] classArr = { "Platform", "SurvivalProperty", "Metadata", "DetectionLimit", "Input", "Frequency",
				"OperatingRange", "CommunicatingDevice", "Condition", "SurvivalRange", "SensorDataSheet", "Service",
				"Sensor", "Amount", "TagDevice", "Coverage", "OperatingPowerRange", "SystemLifetime", "Person",
				"DeviceModule", "Polygon", "BatteryLifetime", "Sensing", "MeasurementRange", "Circle", "NormalUser",
				"SensorOutput", "Group", "SensingDevice", "Admin", "Observation", "DeploymentRelatedProcess", "Output",
				"FeatureOfInterest", "MeasurementProperty", "Attribute", "ResponseTime", "Sensitivity", "IOTSystem",
				"Point", "Selectivity", "MeasurementCapability", "Organization", "Application", "Latency", "Object",
				"Drift", "Stimulus", "Resolution", "Developer", "Unit", "Property", "ObservationValue",
				"ActuatingDevice", "Device", "QuantityKind", "System", "Accuracy", "Deployment", "Precision", "Agent",
				"Process", "OperatingProperty", "MaintenanceSchedule", "Rectangle", "Admin", "check", "Person",
				"Developer", "VirtualSensor", "NormalUser" };

		System.out.println(propArr.length+ "  "+ classArr.length);
		
		OntExtension ont = new OntExtension();
		// System.out.println(ont.checkPropertySimilarity("outputs",propArr));
		System.out.println(ont.checkPropertyMain("hates", "Person", "Person", propArr, classArr));
	}

	public String getBaseForm(String verb) throws IOException {
		String path = "dict/";
		URL url = new URL("file", null, path);
		IDictionary dict = new Dictionary(url);
		dict.open();

		System.setProperty("wordnet.database.dir", "dict");
		WordNetDatabase database = WordNetDatabase.getFileInstance();

		Morphology id = Morphology.getInstance();

		String[] arr = id.getBaseFormCandidates(verb, SynsetType.VERB);
		if (arr.length == 0) {
			// System.out.println("NOT VERB");
			dict.close();
			return "not_verb";
		}
		if (arr.length == 1) {

			// IIndexWord idxWord = dict.getIndexWord(arr[0], POS.VERB);
			// if (idxWord != null) {
			// dict.close();
			// return arr[0];
			// }

			System.err.println("hikkkkkk");
			// return "not_verb";
			if (arr[0].equals("proces")) {
				dict.close();
				return "process";
			}
			if (arr[0].equals("statu")) {
				dict.close();
				return "not_verb";
			}
			dict.close();
			return arr[0];
		}
		if (arr.length > 1) {
			for (String a : arr) {
				IIndexWord idxWord = dict.getIndexWord(a, POS.VERB);
				if (idxWord != null) {
					dict.close();
					return a;
				}
			}

		}
		dict.close();
		return "not_verb";
	}

	// get string from divideName()
	public void assignArrayGivenClass(String[] name) throws IOException {
		nounOfGiven = new ArrayList<String>();
		verbOfGiven = new ArrayList<String>();
		miscOfGiven = new ArrayList<String>();

		// if (isClass) {

		for (String n : name) {

			if (n.equals("of") || n.equals("to") || n.equals("with") || n.equals("on") || n.equals("by")
					|| n.equals("for") || n.equals("iot") || n.equals("a")) {

				// System.out.println("of or on or by or for");
			} else {

				if (isNoun(n)) {
					nounOfGiven.add(n);

				} else {
					// test misc. for the case of Operating (sensing is noun)
					String x = getBaseForm(n);
					if (x.equals("not_verb")) {
						if (!n.equals("of") && !n.equals("to") && !n.equals("with") && !n.equals("on")
								&& !n.equals("by") && !n.equals("for") && !n.equals("iot") && !n.equals("a")) {

							miscOfGiven.add(n);

						}
					} else {

						verbOfGiven.add(x);

					}

				}

			}
		}
		// }

	}

	public void assignArrayGivenProperty(String[] name) throws IOException {
		nounOfGiven = new ArrayList<String>();
		;
		verbOfGiven = new ArrayList<String>();
		;
		miscOfGiven = new ArrayList<String>();
		;
		// System.out.println("dnc");
		// if (isProperty) {

		for (String n : name) {
			String x = getBaseForm(n);
			System.out.println(x);
			if (x.equals("not_verb")) {
				// System.out.println("Not Verb");

				// test if noun or not
				// use kjhfsvj.java if it has a meaning if it's empty then not
				// noun and it's misc.
				// see how to remove it after putting it (no need for that what
				// is not a verb skips it)

				if (isNoun(n)) {
					// System.out.println(isNoun(n)+"1");
					nounOfGiven.add(n);

				}

				if (!isNoun(n)) {

					if (n.equals("of") || n.equals("to") || n.equals("with") || n.equals("on") || n.equals("by")
							|| n.equals("for") || n.equals("iot") || n.equals("a")) {
						System.out.println("of or on or by or for");
					}

					else {
						if (!n.equals("of") && !n.equals("to") && !n.equals("with") && !n.equals("on")
								&& !n.equals("by") && !n.equals("for") && !n.equals("iot") && !n.equals("a")) {
							miscOfGiven.add(n);

						}
					}
				}

				// handle this
				// in out before after last first in misc. array
				// if antynoms and more than 0.5 pass as class or property

			}

			else {

				verbOfGiven.add(x);

			}
		}

		// }
	}

	public void assignArrayExsistingClass(String[] name) throws IOException {
		nounOfExisting = new ArrayList<String>();
		verbOfExisting = new ArrayList<String>();
		miscOfExsisting = new ArrayList<String>();

		// if (isClass) {
		int i = 0;
		int j = 0;
		int k = 0;
		for (String n : name) {

			if (n.equals("of") || n.equals("to") || n.equals("with") || n.equals("on") || n.equals("by")
					|| n.equals("for") || n.equals("iot") || n.equals("a")) {
				System.out.println("of or on or by or for");
			} else {

				if (isNoun(n)) {
					nounOfExisting.add(n);
					i++;
				} else {
					// test misc. for the case of Operating (sensing is noun)
					String x = getBaseForm(n);
					if (x.equals("not_verb")) {
						if (!n.equals("of") && !n.equals("to") && !n.equals("with") && !n.equals("on")
								&& !n.equals("by") && !n.equals("for") && !n.equals("iot") && !n.equals("a")) {

							miscOfExsisting.add(n);
							j++;

						}
					} else {

						verbOfExisting.add(x);

					}

				}

			}
		}
	}

	// }
	public void assignArrayExsistingProperty(String[] name) throws IOException {
		nounOfExisting = new ArrayList<String>();
		verbOfExisting = new ArrayList<String>();
		miscOfExsisting = new ArrayList<String>();

		// if (isProperty) {

		for (String n : name) {
			System.out.println(n);
			String x = getBaseForm(n);

			if (x.equals("not_verb")) {
				System.out.println("Not Verb");

				// test if noun or not
				// use kjhfsvj.java if it has a meaning if it's empty then not
				// noun and it's misc.
				// see how to remove it after putting it (no need for that what
				// is not a verb skips it)

				if (isNoun(n)) {

					nounOfExisting.add(n);

				}

				if (!isNoun(n)) {

					if (n.equals("of") || n.equals("to") || n.equals("with") || n.equals("on") || n.equals("by")
							|| n.equals("for") || n.equals("iot") || n.equals("a")) {
						System.out.println("of or on or by or for");
						// miscOfExsisting[j]=n;
						//
						// j++;
					}

					else {
						if (!n.equals("of") && !n.equals("to") && !n.equals("with") && !n.equals("on")
								&& !n.equals("by") && !n.equals("for") && !n.equals("iot") && !n.equals("a")) {
							miscOfExsisting.add(n);

						}
					}
				}

				// handle this
				// in out before after last first in misc. array
				// if antynoms and more than 0.5 pass as class or property

			}

			else {

				verbOfExisting.add(x);

			}
		}

	}
	// }

	public double[] compareNormal(ArrayList<String> noungiven, ArrayList<String> verbgiven, ArrayList<String> nounex,
			ArrayList<String> verbex) throws SLIB_Exception {
		// Location of WordNet Data
		String dataloc = "dict/";
		// "E:/Lectures/Bachelor/Jena-App/Jena/src/dict//";

		// We create the graph
		URIFactory factory = URIFactoryMemory.getSingleton();
		URI guri = factory.getURI("http://graph/wordnet/");
		G wordnet = new GraphMemory(guri);

		// We load the data into the graph
		GraphLoader_Wordnet loader = new GraphLoader_Wordnet();

		GDataConf dataNoun = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data.noun");
		GDataConf dataVerb = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data.verb");
		// GDataConf dataAdj = new GDataConf(GFormat.WORDNET_DATA, dataloc +
		// "data.adj");
		// GDataConf dataAdv = new GDataConf(GFormat.WORDNET_DATA, dataloc +
		// "data.adv");

		loader.populate(dataNoun, wordnet);
		loader.populate(dataVerb, wordnet);
		// loader.populate(dataAdj, wordnet);
		// loader.populate(dataAdv, wordnet);
		//
		//
		GAction addRoot = new GAction(GActionType.REROOTING);
		GraphActionExecutor.applyAction(addRoot, wordnet);

		ValidatorDAG validatorDAG = new ValidatorDAG();
		Set<URI> roots = validatorDAG.getTaxonomicRoots(wordnet);
		System.out.println("Roots: " + roots);

		String data_noun = dataloc + "index.noun";
		String data_verb = dataloc + "index.verb";

		IndexerWordNetBasic indexWordnetNoun = new IndexerWordNetBasic(factory, wordnet, data_noun);
		IndexerWordNetBasic indexWordnetVerb = new IndexerWordNetBasic(factory, wordnet, data_verb);

		for (Map.Entry<String, Set<URI>> entry : indexWordnetVerb.getIndex().entrySet()) {

			for (URI uri : entry.getValue()) { // foreach verb in the index

				// if the verb has no subsuming verb we link it to the root
				if (wordnet.getE(RDFS.SUBCLASSOF, uri, Direction.OUT).isEmpty()) {
					wordnet.addE(uri, RDFS.SUBCLASSOF, OWL.THING);
				}
			}
		}

		ICconf iconf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
		SMconf measureConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
		measureConf.setICconf(iconf);

		SM_Engine engineSame = new SM_Engine(wordnet);

		double max1 = 0.0;
		double sum1 = 0.0;
		double count1 = 0.0;
		// engine.compare(measureConf,declared.iterator().next(),d2.iterator().next());
		for (int i = 0; i < noungiven.size(); i++) {
			for (int j = 0; j < nounex.size(); j++) {
				Set<URI> uris_iced_coffee = indexWordnetNoun.get(noungiven.get(i));
				Set<URI> uris_iced = indexWordnetNoun.get(nounex.get(j));
				for (URI uri : uris_iced_coffee) {
					for (URI uri1 : uris_iced) {
						// we compute the semantic similarities
						double sim_score = engineSame.compare(measureConf, uri, uri1);

						// since there is multiple definitions for both words
						// then we must loop to get the most similarity

						if (max1 <= sim_score)
							max1 = sim_score;
						// System.out.println(sim_score);
					}
				}
				sum1 += max1;
				count1++;
			}
		}

		double sum2 = 0.0;
		double max2 = 0.0;
		double count2 = 0.0;
		// engine.compare(measureConf,declared.iterator().next(),d2.iterator().next());
		for (int i = 0; i < verbgiven.size(); i++) {
			for (int j = 0; j < verbex.size(); j++) {
				Set<URI> uris_iced_coffee = indexWordnetVerb.get(verbgiven.get(i));
				Set<URI> uris_iced = indexWordnetVerb.get(verbex.get(j));
				for (URI uri : uris_iced_coffee) {
					for (URI uri1 : uris_iced) {
						// we compute the semantic similarities
						double sim_score = engineSame.compare(measureConf, uri, uri1);

						// since there is multiple definitions for both words
						// then we must loop to get the most similarity

						if (max2 <= sim_score)
							max2 = sim_score;

					}
				}
				sum2 += max2;
				count2++;
			}
		}

		ICconf iconf3 = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
		SMconf measureConf3 = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997_NORM);
		measureConf3.setICconf(iconf3);

		SM_Engine engine3 = new SM_Engine(wordnet);

		double max3 = 0.0;
		double count3 = 0.0;
		double sum3 = 0.0;
		// engine.compare(measureConf,declared.iterator().next(),d2.iterator().next());
		for (int i = 0; i < noungiven.size(); i++) {
			for (int j = 0; j < verbex.size(); j++) {
				Set<URI> uris_iced_coffee = indexWordnetNoun.get(noungiven.get(i));
				Set<URI> uris_iced = indexWordnetVerb.get(verbex.get(j));
				for (URI uri : uris_iced_coffee) {
					for (URI uri1 : uris_iced) {
						// we compute the semantic similarities

						double sim_score = engine3.compare(measureConf3, uri, uri1);

						// since there is multiple definitions for both words
						// then we must loop to get the most similarity

						if (max3 <= sim_score)
							max3 = sim_score;
						// since there is multiple definitions for both words
						// then we must loop to get the most similarity

					}
				}
				sum3 += (max3);
				count3++;
			}
		}

		double max4 = 0.0;
		double count4 = 0.0;
		double sum4 = 0.0;
		// engine.compare(measureConf,declared.iterator().next(),d2.iterator().next());
		for (int i = 0; i < verbgiven.size(); i++) {
			for (int j = 0; j < nounex.size(); j++) {
				Set<URI> uris_iced_coffee = indexWordnetVerb.get(verbgiven.get(i));
				Set<URI> uris_iced = indexWordnetNoun.get(nounex.get(j));
				for (URI uri : uris_iced_coffee) {
					for (URI uri1 : uris_iced) {
						// we compute the semantic similarities
						double sim_score = engine3.compare(measureConf3, uri, uri1);

						// since there is multiple definitions for both words
						// then we must loop to get the most similarity

						if (max4 <= sim_score)
							max4 = sim_score;
					}
				}
				sum4 += (max4);
				count4++;
			}
		}

		System.out.println("noun noun " + max1 + " sum1" + sum1);
		double[] result = { sum1 / (noungiven.size() * nounex.size()), sum2 / (verbgiven.size() * verbex.size()),
				(sum3) / (noungiven.size() * verbex.size()), (sum4) / (verbgiven.size() * nounex.size()) };
		// double[] result={sum1,sum2,(sum3),(sum4)};

		return result;

	}

	public boolean isNoun(String noun) throws MalformedURLException {
		// TODO Auto-generated method stub
		String path = "dict/";
		URL url = new URL("file", null, path);

		// construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);

		dict.open();

		// look up first sense of the word "dog"
		IIndexWord idxWord = dict.getIndexWord(noun, POS.NOUN);
		// Iterator<IIndexWord>
		// i=dict.getIndexWordIterator(POS.ADJECTIVE);//loop and compare the get
		// def
		if (!(idxWord == null)) {
			dict.close();
			return true;
		} else {
			dict.close();
			return false;
		}

	}

	public static double[] getRelationsNormal(String givenName, String existingName, ArrayList<String> noungiven,
			ArrayList<String> verbgiven, ArrayList<String> miscgiven, ArrayList<String> nounex,
			ArrayList<String> verbex, ArrayList<String> miscex) throws SLIB_Ex_Critic, SLIB_Exception {
		// noungiven.contains(x); use for this part
		double ant = 0.0;
		double syn = 0.0;
		if ((givenName.contains("before") && existingName.contains("after"))
				|| existingName.contains("before") && givenName.contains("after"))
			ant += 0.01;

		if ((givenName.contains("first") && existingName.contains("last"))
				|| existingName.contains("first") && givenName.contains("last"))
			ant += 0.01;
		if ((givenName.contains("input") && existingName.contains("output"))
				|| existingName.contains("input") && givenName.contains("output")) {
			ant += 0.01;
		} else {
			if ((givenName.contains("in") && existingName.contains("out"))
					|| existingName.contains("in") && givenName.contains("out"))
				ant += 0.01;
		}

		//
		// if((givenName.equals("before")&&existingName.equals("after"))||existingName.equals("before")&&givenName.equals("after"))
		// return 0.1;
		// if((givenName.equals("in")&&existingName.equals("out"))||existingName.equals("in")&&givenName.equals("out"))
		// return 0.1;
		// if((givenName.equals("first")&&existingName.equals("last"))||existingName.equals("first")&&givenName.equals("last"))
		// return 0.1;
		String dataloc = "dict/";

		String[] functions = { "noun", "verb", "adj", "adv" };

		URIFactory factory = URIFactoryMemory.getSingleton();
		URI guri = factory.getURI("http://graph/wordnet/");
		G wordnet = new GraphMemory(guri);

		// We load WordNet as well as an index
		GraphLoader_Wordnet_Full wordnetLoader = new GraphLoader_Wordnet_Full();
		IndexerWordNetBasic indexWordnetWordUris = new IndexerWordNetBasic(wordnet);

		for (String function : functions) {
			GDataConf data = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data." + function);
			wordnetLoader.populate(data, wordnet);
			indexWordnetWordUris.populateIndex(factory, dataloc + "index." + function);
		}

		// reverse index to be able to retrieve the labels associated to an URI

		Map<URI, Set<String>> invertedIndex = new HashMap();
		for (Map.Entry<String, Set<URI>> e : indexWordnetWordUris.getIndex().entrySet()) {
			String label = e.getKey();
			Set<URI> uris = e.getValue();

			for (URI u : uris) {
				if (!invertedIndex.containsKey(u)) {
					invertedIndex.put(u, new HashSet<String>());
				}
				invertedIndex.get(u).add(label);
			}
		}

		// print the graph
		// for (E e : wordnet.getE()) {
		// System.out.println(e);
		// }
		URI similarTo = GraphLoader_Wordnet_Full.pointerSymbolsToURIs.get("&");
		URI opposite = GraphLoader_Wordnet_Full.pointerSymbolsToURIs.get("!");
		// or URI similarTo = factory.getURI("http://SML/wordNet/SimilarTo");
		double s1 = 0.0;
		double s2 = 0.0;
		double s3 = 0.0;
		double cs1 = 0.0;
		double cs2 = 0.0;
		double cs3 = 0.0;
		for (E e : wordnet.getE(similarTo)) {

			URI source = e.getSource();
			URI target = e.getTarget();

			// System.out.println(e);
			String firstLabelSource = (invertedIndex.containsKey(source)) ? invertedIndex.get(source).iterator().next()
					: "NO_LABEL (SENSE)";
			String firstLabelTarget = (invertedIndex.containsKey(target)) ? invertedIndex.get(target).iterator().next()
					: "NO_LABEL (SENSE)";

			if ((invertedIndex.containsKey(source))) {
				for (String string : invertedIndex.get(source)) {

					if ((invertedIndex.containsKey(target))) {
						for (String string2 : invertedIndex.get(target)) {
							for (int i = 0; i < noungiven.size(); i++) {
								for (int j = 0; j < nounex.size(); j++) {

									if (string.equals(noungiven.get(i)) && string2.equals(nounex.get(j))) {
										// boolean c=true;
										// return 0.10;
										s1 += 0.7;

									}
									cs1++;
								}
							}

							for (int i = 0; i < verbgiven.size(); i++) {
								for (int j = 0; j < verbex.size(); j++) {
									if (string.equals(verbgiven.get(i)) && string2.equals(verbex.get(j))) {
										s2 += 0.7;

									}
									cs2++;
								}
							}
							for (int i = 0; i < miscgiven.size(); i++) {
								for (int j = 0; j < miscex.size(); j++) {
									if (string.equals(miscgiven.get(i)) && string2.equals(miscex.get(j))) {
										s3 += 0.7;

									}
									cs3++;
								}
							}

							// if(string.equals(givenName)&&string2.equals(existingName)){
							// boolean c=true;
							// //return 0.10;
							//
							// }

						}
					}
				}
			}

		}

		double a1 = 0.0;
		double a2 = 0.0;
		double a3 = 0.0;
		double ca1 = 0.0;
		double ca2 = 0.0;
		double ca3 = 0.0;
		for (E e : wordnet.getE(opposite)) {

			URI source = e.getSource();
			URI target = e.getTarget();

			// System.out.println(e);
			String firstLabelSource = (invertedIndex.containsKey(source)) ? invertedIndex.get(source).iterator().next()
					: "NO_LABEL (SENSE)";
			String firstLabelTarget = (invertedIndex.containsKey(target)) ? invertedIndex.get(target).iterator().next()
					: "NO_LABEL (SENSE)";

			if ((invertedIndex.containsKey(source))) {
				for (String string : invertedIndex.get(source)) {

					if ((invertedIndex.containsKey(target))) {
						for (String string2 : invertedIndex.get(target)) {

							for (int i = 0; i < noungiven.size(); i++) {
								for (int j = 0; j < nounex.size(); j++) {

									if (string.equals(noungiven.get(i)) && string2.equals(nounex.get(j))) {
										// boolean c=true;
										// return 0.10;
										s1 += 0.01;

									}
									ca1++;
								}
							}

							for (int i = 0; i < verbgiven.size(); i++) {
								for (int j = 0; j < verbex.size(); j++) {
									if (string.equals(verbgiven.get(i)) && string2.equals(verbex.get(j))) {
										s2 += 0.01;

									}
									ca2++;
								}
							}
							for (int i = 0; i < miscgiven.size(); i++) {
								for (int j = 0; j < miscex.size(); j++) {
									if (string.equals(miscgiven.get(i)) && string2.equals(miscex.get(j))) {
										s3 += 0.01;

									}
									ca3++;
								}
							}

							// if(string.equals(givenName)&&string2.equals(existingName)){
							// boolean c=true;
							// return 0.10;
							//
							// }

						}
					}
				}
			}

		}
		// if any is zero don't divide
		double[] result = { s1, s2, s3, a1, a2, a3, ant };

		return result;

	}

}
