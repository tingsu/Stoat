/*
 * Copyright (C) 2011 Nephoapp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nephoapp.anarxiv;

import java.util.Collection;
import java.util.TreeMap;

/**
 * This class stores url and corresponding descriptions that will be displayed 
 * on the gui.
 * 
 *
 */
public class UrlTable 
{
	/** main category. */
	public final static String[] Category = {"Astrophysics", 			// 0
											 "Condensed Matter",		// 1
											 "Physics",					// 2
											 "High Energy Physics",		// 3
											 "Nuclear",					// 4
											 "Mathematics",				// 5
											 "Nonlinear Sciences",		// 6
											 "Computer Science",		// 7
											 "Quantitative Biology",	// 8
											 "Quantitative Finance",	// 9
											 "Statistics"				// 10
											 };
	
	/** the BIG MAP, mapping subcategories. */
	private TreeMap<String, TreeMap<String, String>> _BigMap = new TreeMap<String, TreeMap<String, String>>();
	
	/** sub category: astrophysics. */
	private TreeMap<String, String> _UrlMap_Astrophysics = new TreeMap<String, String>();
	
	/** sub category: condensed matter. */
	private TreeMap<String, String> _UrlMap_CondensedMatter = new TreeMap<String, String>();
	
	/** sub category: high energy physics. */
	private TreeMap<String, String> _UrlMap_HEP = new TreeMap<String, String>();
	
	/** sub category: nuclear. */
	private TreeMap<String, String> _UrlMap_Nuclear = new TreeMap<String, String>();
	
	/** sub category: physics. */
	/* TODO: missing: General Relativity and Quantum Cosmology. */
	private TreeMap<String, String> _UrlMap_Physics = new TreeMap<String, String>();
	
	/** sub category: mathematics. */
	private TreeMap<String, String> _UrlMap_Math = new TreeMap<String, String>();
	
	/** sub category: nonlinear science. */
	private TreeMap<String, String> _UrlMap_NonlinearSci = new TreeMap<String, String>();
	
	/** sub category: computer science. */
	private TreeMap<String, String> _UrlMap_CS = new TreeMap<String, String>();
	
	/** sub category: quantitative biology. */
	private TreeMap<String, String> _UrlMap_QuantBio = new TreeMap<String, String>();
	
	/** sub category: quantitative finance. */
	private TreeMap<String, String> _UrlMap_QuantFinance = new TreeMap<String, String>();
	
	/** sub category: statistics. */
	private TreeMap<String, String> _UrlMap_Statistics = new TreeMap<String, String>();
	
	/** 
	 * constructor.
	 * maps are built here.
	 */
	public UrlTable()
	{
		this.buildSubcategoryMap();
		this.buildUrlMap();
	}
	
	/**
	 * build _SubcategoryMap.
	 */
	private void buildSubcategoryMap()
	{
		this._BigMap.put(UrlTable.Category[0], this._UrlMap_Astrophysics);
		this._BigMap.put(UrlTable.Category[1], this._UrlMap_CondensedMatter);
		this._BigMap.put(UrlTable.Category[2], this._UrlMap_Physics);
		this._BigMap.put(UrlTable.Category[3], this._UrlMap_HEP);
		this._BigMap.put(UrlTable.Category[4], this._UrlMap_Nuclear);
		this._BigMap.put(UrlTable.Category[5], this._UrlMap_Math);
		this._BigMap.put(UrlTable.Category[6], this._UrlMap_NonlinearSci);
		this._BigMap.put(UrlTable.Category[7], this._UrlMap_CS);
		this._BigMap.put(UrlTable.Category[8], this._UrlMap_QuantBio);
		this._BigMap.put(UrlTable.Category[9], this._UrlMap_QuantFinance);
		this._BigMap.put(UrlTable.Category[10], this._UrlMap_Statistics);
	}
	
	/**
	 * build _UrlMap.
	 */
	private void buildUrlMap()
	{
		/* astrophysics. */
//		this._UrlMap_Astrophysics.put("All Astrophysics", 				"astro-ph");
		this._UrlMap_Astrophysics.put("Cosmology and Extragalactic", 	"astro-ph.CO");
		this._UrlMap_Astrophysics.put("Earth and Planetary", 			"astro-ph.EP");
		this._UrlMap_Astrophysics.put("Galaxy", 						"astro-ph.GA");
		this._UrlMap_Astrophysics.put("High Energy Phenomena", 			"astro-ph.HE");
		this._UrlMap_Astrophysics.put("Instrumentation and Methods", 	"astro-ph.IM");
		this._UrlMap_Astrophysics.put("Solar and Stellar", 				"astro-ph.SR");
		
		/* condensed matter. */
//		this._UrlMap_CondensedMatter.put("All Condensed Matter",					"cond-mat");
		this._UrlMap_CondensedMatter.put("Disordered Systems and Neural Networks",	"cond-mat.dis-nn");
		this._UrlMap_CondensedMatter.put("Materials Science", 						"cond-mat.mtrl-sci");
		this._UrlMap_CondensedMatter.put("Mesoscale and Nanoscale Physics", 		"cond-mat.mes-hall");
		this._UrlMap_CondensedMatter.put("Other Condensed Matter", 					"cond-mat.other");
		this._UrlMap_CondensedMatter.put("Quantum Gases", 							"cond-mat.quant-gas");
		this._UrlMap_CondensedMatter.put("Soft Condensed Matter", 					"cond-mat.soft");
		this._UrlMap_CondensedMatter.put("Statistical Mechanics", 					"cond-mat.stat-mech");
		this._UrlMap_CondensedMatter.put("Strongly Correlated Electrons", 			"cond-mat.str-el");
		this._UrlMap_CondensedMatter.put("Superconductivity", 						"cond-mat.supr-con");
		
		/* high energy physics. */
		this._UrlMap_HEP.put("Experiment",		"hep-ex");
		this._UrlMap_HEP.put("Lattice",			"hep-lat");
		this._UrlMap_HEP.put("Phenomenology",	"hep-ph");
		this._UrlMap_HEP.put("Theory",			"hep-th");
		
		/* nuclear. */
		this._UrlMap_Nuclear.put("Experiment", 	"nucl-ex");
		this._UrlMap_Nuclear.put("Theory", 		"nucl-th");
		
		/* physics. */
//		this._UrlMap_Physics.put("All Physics", 								"physics");
		this._UrlMap_Physics.put("Accelerator Physics", 						"physics.acc-ph");
		this._UrlMap_Physics.put("Atmospheric and Oceanic Physics", 			"physics.ao-ph");
		this._UrlMap_Physics.put("Atomic Physics", 								"physics.atom-ph");
		this._UrlMap_Physics.put("Atomic and Molecular Clusters", 				"physics.atm-clus");
		this._UrlMap_Physics.put("Biological Physics", 							"physics.bio-ph");
		this._UrlMap_Physics.put("Chemical Physics", 							"physics.chem-ph");
		this._UrlMap_Physics.put("Classical Physics", 							"physics.class-ph");
		this._UrlMap_Physics.put("Computational Physics", 						"physics.comp-ph");
		this._UrlMap_Physics.put("Data Analysis, Statistics and Probability", 	"physics.data-an");
		this._UrlMap_Physics.put("Fluid Dynamics", 								"physics.flu-dyn");
		this._UrlMap_Physics.put("General Physics", 							"physics.gen-ph");
		this._UrlMap_Physics.put("Geophysics", 									"physics.geo-ph");
		this._UrlMap_Physics.put("History of Physics", 							"physics.hist-ph");
		this._UrlMap_Physics.put("Instrumentation and Detectors", 				"physics.ins-det");
		this._UrlMap_Physics.put("Medical Physics", 							"physics.med-ph");
		this._UrlMap_Physics.put("Optics", 										"physics.optics");
		this._UrlMap_Physics.put("Physics Education", 							"physics.ed-ph");
		this._UrlMap_Physics.put("Physics and Society", 						"physics.soc-ph");
		this._UrlMap_Physics.put("Plasma Physics", 								"physics.plasm-ph");
		this._UrlMap_Physics.put("Popular Physics", 							"physics.pop-ph");
		this._UrlMap_Physics.put("Space Physics", 								"physics.space-ph");
		
		/* mathematics. */
//		this._UrlMap_Math.put("All Mathematics", 				"math");
		this._UrlMap_Math.put("Algebraic Geometry", 			"math.AG");
		this._UrlMap_Math.put("Algebraic Topology", 			"math.AT");
		this._UrlMap_Math.put("Analysis of PDEs", 				"math.AP");
		this._UrlMap_Math.put("Category Theory", 				"math.CT");
		this._UrlMap_Math.put("Classical Analysis and ODEs", 	"math.CA");
		this._UrlMap_Math.put("Combinatorics", 					"math.CO");
		this._UrlMap_Math.put("Commutative Algebra", 			"math.AC");
		this._UrlMap_Math.put("Complex Variables", 				"math.CV");
		this._UrlMap_Math.put("Differential Geometry", 			"math.DG");
		this._UrlMap_Math.put("Dynamical Systems", 				"math.DS");
		this._UrlMap_Math.put("Functional Analysis", 			"math.FA");
		this._UrlMap_Math.put("General Mathematics", 			"math.GM");
		this._UrlMap_Math.put("General Topology", 				"math.GN");
		this._UrlMap_Math.put("Geometric Topology", 			"math.GT");
		this._UrlMap_Math.put("Group Theory", 					"math.GR");
		this._UrlMap_Math.put("History and Overview", 			"math.HO");
		this._UrlMap_Math.put("Information Theory", 			"math.IT");
		this._UrlMap_Math.put("K-Theory and Homology", 			"math.KT");
		this._UrlMap_Math.put("Logic", 							"math.LO");
		this._UrlMap_Math.put("Mathematical Physics", 			"math.MP");
		this._UrlMap_Math.put("Metric Geometry", 				"math.MG");
		this._UrlMap_Math.put("Number Theory", 					"math.NT");
		this._UrlMap_Math.put("Numerical Analysis", 			"math.NA");
		this._UrlMap_Math.put("Operator Algebras", 				"math.OA");
		this._UrlMap_Math.put("Optimization and Control", 		"math.OC");
		this._UrlMap_Math.put("Probability", 					"math.PR");
		this._UrlMap_Math.put("Quantum Algebra", 				"math.QA");
		this._UrlMap_Math.put("Representation Theory", 			"math.RT");
		this._UrlMap_Math.put("Rings and Algebras", 			"math.RA");
		this._UrlMap_Math.put("Spectral Theory", 				"math.SP");
		this._UrlMap_Math.put("Statistics Theory", 				"math.ST");
		this._UrlMap_Math.put("Symplectic Geometry", 			"math.SG");
		
		/* nonlinear science. */
//		this._UrlMap_NonlinearSci.put("All Nonlinear Science", 						"nlin");
		this._UrlMap_NonlinearSci.put("Adaptation and Self-Organizing Systems", 	"nlin.AO");
		this._UrlMap_NonlinearSci.put("Cellular Automata and Lattice Gases", 		"nlin.CG");
		this._UrlMap_NonlinearSci.put("Chaotic Dynamics", 							"nlin.CD");
		this._UrlMap_NonlinearSci.put("Exactly Solvable and Integrable Systems", 	"nlin.SI");
		this._UrlMap_NonlinearSci.put("Pattern Formation and Solitons", 			"nlin.PS");
		
		/* computer science. */	   
//		this._UrlMap_CS.put("All Computer Science", 							"cs");
		this._UrlMap_CS.put("Artificial Intelligence", 							"cs.AI");
		this._UrlMap_CS.put("Computation and Language", 						"cs.CL");
		this._UrlMap_CS.put("Computational Complexity", 						"cs.CC");
		this._UrlMap_CS.put("Computational Engineering, Finance, and Science", 	"cs.CE");
		this._UrlMap_CS.put("Computational Geometry", 							"cs.CG");
		this._UrlMap_CS.put("Computer Science and Game Theory", 				"cs.GT");
		this._UrlMap_CS.put("Computer Vision and Pattern Recognition", 			"cs.CV");
		this._UrlMap_CS.put("Computers and Society", 							"cs.CY");
		this._UrlMap_CS.put("Cryptography and Security", 						"cs.CR");
		this._UrlMap_CS.put("Data Structures and Algorithms", 					"cs.DS");
		this._UrlMap_CS.put("Databases", 										"cs.DB");
		this._UrlMap_CS.put("Digital Libraries", 								"cs.DL");
		this._UrlMap_CS.put("Discrete Mathematics", 							"cs.DM");
		this._UrlMap_CS.put("Distributed, Parallel, and Cluster Computing", 	"cs.DC");
		this._UrlMap_CS.put("Formal Languages and Automata Theory", 			"cs.FL");
		this._UrlMap_CS.put("General Literature", 								"cs.GL");
		this._UrlMap_CS.put("Graphics", 										"cs.GR");
		this._UrlMap_CS.put("Hardware Architecture", 							"cs.AR");
		this._UrlMap_CS.put("Human-Computer Interaction", 						"cs.HC");
		this._UrlMap_CS.put("Information Retrieval", 							"cs.IR");
		this._UrlMap_CS.put("Information Theory", 								"cs.IT");
		this._UrlMap_CS.put("Learning", 										"cs.LG");
		this._UrlMap_CS.put("Logic in Computer Science", 						"cs.LO");
		this._UrlMap_CS.put("Mathematical Software", 							"cs.MS");
		this._UrlMap_CS.put("Multiagent Systems", 								"cs.MA");
		this._UrlMap_CS.put("Multimedia", 										"cs.MM");
		this._UrlMap_CS.put("Networking and Internet Architecture", 			"cs.NI");
		this._UrlMap_CS.put("Neural and Evolutionary Computing", 				"cs.NE");
		this._UrlMap_CS.put("Numerical Analysis", 								"cs.NA");
		this._UrlMap_CS.put("Operating Systems", 								"cs.OS");
		this._UrlMap_CS.put("Other Computer Science", 							"cs.OH");
		this._UrlMap_CS.put("Performance", 										"cs.PF");
		this._UrlMap_CS.put("Programming Languages", 							"cs.PL");
		this._UrlMap_CS.put("Robotics", 										"cs.RO");
		this._UrlMap_CS.put("Social and Information Networks", 					"cs.SI");
		this._UrlMap_CS.put("Software Engineering", 							"cs.SE");
		this._UrlMap_CS.put("Sound", 											"cs.SD");
		this._UrlMap_CS.put("Symbolic Computation", 							"cs.SC");
		this._UrlMap_CS.put("Systems and Control", 								"cs.SY");
		
		/* quantitative biology. */
//		this._UrlMap_QuantBio.put("All Quant Bio",				"q-bio");
		this._UrlMap_QuantBio.put("Biomolecules",				"q-bio.BM");
		this._UrlMap_QuantBio.put("Cell Behavior",				"q-bio.CB");
		this._UrlMap_QuantBio.put("Genomics",					"q-bio.GN");
		this._UrlMap_QuantBio.put("Molecular Networks",			"q-bio.MN");
		this._UrlMap_QuantBio.put("Neurons and Cognition",		"q-bio.NC");
		this._UrlMap_QuantBio.put("Other Quantitative Biology",	"q-bio.OT");
		this._UrlMap_QuantBio.put("Populations and Evolution",	"q-bio.PE");
		this._UrlMap_QuantBio.put("Quantitative Methods",		"q-bio.QM");
		this._UrlMap_QuantBio.put("Subcellular Processes",		"q-bio.SC");
		this._UrlMap_QuantBio.put("Tissues and Organs",			"q-bio.TO");
		
		/* quantitative finance. */
//		this._UrlMap_QuantFinance.put("All Quant Finance", 					"q-fin");
		this._UrlMap_QuantFinance.put("Computational Finance", 				"q-fin.CP");
		this._UrlMap_QuantFinance.put("General Finance", 					"q-fin.GN");
		this._UrlMap_QuantFinance.put("Portfolio Management", 				"q-fin.PM");
		this._UrlMap_QuantFinance.put("Pricing of Securities", 				"q-fin.PR");
		this._UrlMap_QuantFinance.put("Risk Management", 					"q-fin.RM");
		this._UrlMap_QuantFinance.put("Statistical Finance", 				"q-fin.ST");
		this._UrlMap_QuantFinance.put("Trading and Market Microstructure", 	"q-fin.TR");
		
		/* statistics. */		   
//		this._UrlMap_Statistics.put("All Statistics", 		"stat");
		this._UrlMap_Statistics.put("Applications", 		"stat.AP");
		this._UrlMap_Statistics.put("Computation", 			"stat.CO");
		this._UrlMap_Statistics.put("Machine Learning", 	"stat.ML");
		this._UrlMap_Statistics.put("Methodology", 			"stat.ME");
		this._UrlMap_Statistics.put("Other Statistics", 	"stat.OT");
		this._UrlMap_Statistics.put("Statistics Theory", 	"stat.TH");
	}
	
	/**
	 * get the main category list.
	 */
	public String[] getMainCategoryList()
	{
		String[] mainCategoryNames = _BigMap.keySet().toArray(new String[0]);
		return mainCategoryNames;
	}
	
	/**
	 * get the subcategory list of a main category.
	 */
	public String[] getSubcategoryList(String mainCat)
	{
		TreeMap<String, String> subcat = this._BigMap.get(mainCat);
		String[] keys = subcat.keySet().toArray(new String[0]);
		return keys;
	}
	
	/**
	 * get the query string for a subcategory item.
	 */
	public String getQueryString(String subCatName)
	{
		Collection<TreeMap<String, String>> subcatList = this._BigMap.values();
		
		for(TreeMap<String, String> subcat: subcatList)
		{
			if(subcat.containsKey(subCatName) == true)
			{
				return subcat.get(subCatName);
			}
		}
		
		return null;
	}
	
	/**
	 * make the query url.
	 */
	public static String makeQueryUrl(String subCatName, int start, int maxResults)
	{
		String url = "http://export.arxiv.org/api/query?search_query=cat:" + subCatName + 
					 "&sortBy=submittedDate&sortOrder=descending&start=" + start +
					 "&max_results=" + maxResults;
		return url;
	}
	
	/**
	 * make id query url.
	 */
	public static String makeQueryByIdUrl(String id)
	{
		String url = "http://export.arxiv.org/api/query?id_list=" + id;
		return url;
	}
}
