package evalg.doors.gen;

import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.app.App;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;


public class MainClass {
	
	public static int getGenIndex(double r, int max) {
		/*
		 * Lo hago como funcion, para encapsular, y as� dejar m�s 
		 * limpio el codigo y hacerlo m�s escalable.
		 * */
		return (int) Math.floor(max*r);
	}
	
	public static ArrayList<Genome> runSimulation(
			HeadlessWorkspace wkspc,
			String path, 
			ArrayList<Genome> generation, 
			int gen_idx) 
	{
		ArrayList<Genome> best_worst = new ArrayList<>();
		int subject_idx = 0;
		for(Genome subject:generation) {
			String doorsFilePath = path+"generation_"+gen_idx+"/subj_"+subject_idx+"/doors.plan";
			try {
				PrintWriter doorsFile = new PrintWriter(doorsFilePath, "UTF-8");
				for (int i = 0; i < subject.getSize(); i++) {
					Gen gen = subject.getGen(i);
					doorsFile.println(gen.x+" "+gen.y);
				}
				doorsFile.close();
				wkspc.command("load-fixed-plan-file"); 
				wkspc.command("load-fixed-door-file");
				wkspc.command("generate-population");                                    
				wkspc.command("repeat 200 [ go ]");            
				wkspc.dispose();
				wkspc.clearAll();
				
				File ticks = new File(wkspc+"NLFiles/"+"seconds.output");
				Scanner reader = new Scanner(ticks);
				subject.setFitness(reader.nextInt());
				reader.close();
				
				if(best_worst.size() < 1) {
					//al comienzo el primer sujeto es el mejor y el peor
					best_worst.add(subject);
					best_worst.add(subject);
				}else{
					best_worst.set(0, subject.lesser(best_worst.get(0)));
					best_worst.set(1, subject.greater(best_worst.get(1)));
				}
			}catch (UnsupportedEncodingException uee) {
				uee.printStackTrace();
			}catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			}catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
        return best_worst;
	}
	
	public static boolean run_netlogo(String escapeFileString) {
		boolean success=true;
		String[] argv= {};
		App.main(argv);
		try {
	      java.awt.EventQueue.invokeAndWait(
			new Runnable() {
			  public void run() {
			    try {
			    	App.app().open(escapeFileString);
			    }
			    catch(java.io.IOException ex) {
			      ex.printStackTrace();
			    }}});
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
		return success;
	}

	public static void main(String[] args) {
		String wkspc = new File("").getAbsolutePath()+"/src/";
		String [] buildingFiles = {"conference.plan","office.plan","office.plan"};
		int generation_size = 20; //min is 4
		int genome_size = 5; //5 doors
		//calculate random population size for NL, so all runs in 1 compile will be tested on the same population size
		//int population_size = ((int) Math.random()*30)*10;
		int cant_runs = 1;//500;
		HeadlessWorkspace NLworkspace = HeadlessWorkspace.newInstance();
		try {
            NLworkspace.open(wkspc+"NLFiles/escape4_v6.nlogo");
		
			for(String buildingFile: buildingFiles) {
				//la idea es: por cada archivo, parsear los puntos y ponerlos en contenedores distintos
				ArrayList<Gen> outside = new ArrayList<>();
				ArrayList<Gen> inside = new ArrayList<>();
				ArrayList<Gen> wall = new ArrayList<>();
				String buildingPath = wkspc+"NLFiles/"+buildingFile;
				File buildingPlan = new File(buildingPath);
				File NLPlan = new File(wkspc+"NLFiles/plan.plan");
				try {
					Scanner reader = new Scanner(buildingPlan);
					PrintWriter planFile = new PrintWriter(NLPlan, "UTF-8");
					while(reader.hasNextLine()) {
						String raw_line = reader.nextLine();
						planFile.println(raw_line);
						String [] line = raw_line.split(" ");
						
						switch (Integer.parseInt(line[2])) {
						case 0:
							inside.add(new Gen(Integer.parseInt(line[0]),Integer.parseInt(line[1])));
							break;
						case 2:
							outside.add(new Gen(Integer.parseInt(line[0]),Integer.parseInt(line[1])));
							break;
						case 64:
							wall.add(new Gen(Integer.parseInt(line[0]),Integer.parseInt(line[1])));
							break;
						}
					}
					reader.close();
					planFile.close();
					//luego, generar 5 numeros aleatorios Ri (i=1..5), tal que 
					// si |wall| = Nw, y f(x) : Dom(R)->[0,Nw]; f(x) = mx+b, y R e [0,1]
					// plt, f(x) = Nw*x
					ArrayList<ArrayList<Genome>> generations = new ArrayList<>();
					generations.add(new ArrayList<>());
					for (int i = 0; i < generation_size; i++) {
						Genome genome = new Genome(genome_size);
						for (int j = 0; j < genome_size; j++) {
							int gen_idx = getGenIndex(Math.random(),wall.size());
							while (genome.exist(gen_idx)) gen_idx = getGenIndex(Math.random(),wall.size());
							genome.addGen(wall.get(gen_idx), gen_idx);
						}
						generations.get(0).add(genome);
					}
					int best_time = 0;
					int res_file_idx = 0;
					File resultsFile = new File(wkspc+"results"+".txt");
					while (resultsFile.exists()) resultsFile = new File(wkspc+"results"+(++res_file_idx)+".txt");
					PrintWriter resultsFileW = new PrintWriter(resultsFile);
					do {
						resultsFileW.print("Generacion "+(generations.size()-1)+" ");
						//select best & worst (by running the simulation, also, the fitness of all subjects is calculated
						ArrayList<Genome> b_w = runSimulation(NLworkspace, wkspc, generations.get(generations.size()-1), generations.size()-1);
						if (b_w.get(0).getFitness() > best_time) {best_time = b_w.get(0).getFitness();}
						generations.add(new ArrayList<>());
						generations.get(generations.size()-1).addAll(b_w);
						//generate random
						//the number of random elements to pick will be calculated by floor((generatiom_size-1)/genome_size)
						int cant_rand = Math.floorDiv(generation_size-1, genome_size);
						for (int i = 0; i < cant_rand; i++) {
							Genome rand_subject = new Genome(genome_size);
							for (int j = 0; j < genome_size; j++) {
								int gen_idx = getGenIndex(Math.random(),wall.size());
								while (rand_subject.exist(gen_idx)) gen_idx = getGenIndex(Math.random(),wall.size());
								rand_subject.addGen(wall.get(gen_idx), gen_idx);
							}
							generations.get(generations.size()-1).add(rand_subject);
						}
						//to calculate the amount of individuals to reproduce and mutate
						//first, the current percentage of used genes needs to be calculated
						//then, to that amount, 70% (approximated to the lower) are to be reproduced,
						//and 30% (approximated to the higher) are to mutate.
						int cant_rep = (int) Math.floor((generation_size-cant_rand-2)*0.7);
						int cant_mut = (int) Math.ceil((generation_size-cant_rand-2)*0.3); 
						//order subject by fitness
						Collections.sort(generations.get(generations.size()-2));
						for(Genome subject:generations.get(generations.size()-2)) {
							resultsFileW.print(subject.getFitness()+" ");
						}
						resultsFileW.println(best_time);
						//select for mutation
						ArrayList<Genome> experiments = selectForMutation(generations.get(generations.size()-2), cant_mut);
						//mutate
						ArrayList<Genome> mutants = mutate(experiments, wall, genome_size);
						//select for reproduction
						//for the tournament group size, cant_rand+1 will be used
						ArrayList<Genome> parents = selectForReproduction(generations.get(generations.size()-2), cant_rand, cant_rep);
						//reproduce
						ArrayList<Genome> children = reproduce(parents, wall, genome_size);
						//compile
						generations.get(generations.size()-1).addAll(mutants);
						generations.get(generations.size()-1).addAll(children);
					}while(--cant_runs > 0);//stop at 1 second of difference (by now)
					resultsFileW.println("Best Time: "+best_time);
					resultsFileW.close();
				}catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			NLworkspace.dispose();
		}catch(Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public static ArrayList<Genome> selectForMutation(ArrayList<Genome> generation, int k){
		//para este proceso se utilizr� la metodolog�a SUS, combinada con ranking
		//Se supone que el genoma viene reordenado en forma ascendente, donde 0 es el mejor y n el peor.
		//el largo de la lista final esta dado por (k^2+k)/2 (numeros triangulares)
		ArrayList<Genome> forMutation =  new ArrayList<>();
		//se genera una lista con n 0, n-1 1, n-2 2, etc.
		ArrayList<Integer> roulette = new ArrayList<>();
		for (int i = generation.size(); i >= 1; i--) {
			for (int j = 0; j < i; j++) {
				roulette.add(j);
			}
		}
		//se hacer "girar" -> se determina el pivote
		int pivot = (int) Math.random()*roulette.size();
		//mover roullete y selecionar �ndices segun 'k'
		roulette.addAll(roulette.subList(0,pivot));
		roulette.subList(0,pivot).clear();
		//seleccionar k valores equidistantes
		//y agregar a  'forMutation
		for	(int i = 0; i < roulette.size(); i += (int) roulette.size()/k) {
			forMutation.add(generation.get(roulette.get(i)-1));
		}
		return forMutation;
	}
	
	public static ArrayList<Genome> selectForReproduction(ArrayList<Genome> generation, int k, int n){
		//para este proceso se utilizar� la metodolog�a de torneo 
		//donde k es el tama�o de los grupos y n la cantidad de individuos a generar
		ArrayList<Genome> forReproduction = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Genome subject = generation.get((int) Math.random()*generation.size());
			for (int j = 1; j < k; j++) subject = subject.lesser(generation.get((int) Math.random()*generation.size()));
			forReproduction.add(subject);
		}
		
		return forReproduction;
	}
	
	public static ArrayList<Genome> mutate(ArrayList<Genome> e_subjects, ArrayList<Gen> ctx, int g_size) {
		ArrayList<Genome> mutated = new ArrayList<Genome>();
		for (Genome subject: e_subjects) {
			mutated.add(new Genome(g_size));
			//selects n genes at random to be mutated
			int n_mut = (int) Math.random()*(g_size-1)+1;
			boolean [] usedGenesIdxs = new boolean[g_size];
			for (int i = 0; i < g_size; i++) usedGenesIdxs[i] = false;
			for (int i = 0; i < n_mut;i++) {
				//selects a random gen
				int rand_i = (int) Math.random()*(g_size);
				//makes sure it hasn't been mutated
				while (usedGenesIdxs[rand_i]) rand_i = (int) Math.random()*(g_size);
				//now goes up, right, down, left around the gen, and pick the first valid
				for (int j = 0; j < ctx.size(); j++){
					//up
					if(subject.getGen(rand_i).x == ctx.get(j).x && subject.getGen(rand_i).y+1 == ctx.get(j).y) {
						if (!mutated.get(mutated.size()-1).exist(j)) {
							usedGenesIdxs[rand_i] = true;
							mutated.get(mutated.size()-1).addGen(new Gen(subject.getGen(rand_i).x,subject.getGen(rand_i).y+1), j);
							break;
						}//me preocupa lo que pasar�a si hay 5 puertas seguidas -> evaluar
					}
					//right
					if(subject.getGen(rand_i).x+1 == ctx.get(j).x && subject.getGen(rand_i).y == ctx.get(j).y) {
						if (!mutated.get(mutated.size()-1).exist(j)) {
							usedGenesIdxs[rand_i] = true;
							mutated.get(mutated.size()-1).addGen(new Gen(subject.getGen(rand_i).x+1,subject.getGen(rand_i).y), j);
							break;
						}
					}
					//down
					if(subject.getGen(rand_i).x == ctx.get(j).x && subject.getGen(rand_i).y-1 == ctx.get(j).y) {
						if (!mutated.get(mutated.size()-1).exist(j)) {
							usedGenesIdxs[rand_i] = true;
							mutated.get(mutated.size()-1).addGen(new Gen(subject.getGen(rand_i).x,subject.getGen(rand_i).y-1), j);
							break;
						}
					}
					//left
					if(subject.getGen(rand_i).x-1 == ctx.get(j).x && subject.getGen(rand_i).y == ctx.get(j).y) {
						if (!mutated.get(mutated.size()-1).exist(j)) {
							usedGenesIdxs[rand_i] = true;
							mutated.get(mutated.size()-1).addGen(new Gen(subject.getGen(rand_i).x-1,subject.getGen(rand_i).y), j);
							break;
						}
					}
				}
			}
		}
		return mutated;
	}
	
	public static ArrayList<Genome> reproduce(ArrayList<Genome> p_subjects, ArrayList<Gen> ctx, int g_size) {
		//to get the offspring of two genes, the pondered mean will be used
		//by adding up both fitness values and then adding like gen1.x*((1/gen1_fit)/added_fit)+gen2.x*((1/gen2_fit)/added_fit)
		//and analog for 'y' value, getting something like an embryo or proto state of the child. 
		//Then the first closest gene in ctx will be chosen as child.		
		//All subjects in p_subjects will be at least fathers, though mothers will be chosen at random,
		//meaning one subject may be mother more than one time.
		ArrayList<Genome> offsprings = new ArrayList<Genome>();
		for (int i = 0; i < p_subjects.size(); i++) {
			offsprings.add(new Genome(g_size));
			//to take the father, the first element of p_subjects will be removed,
			//so the rest will be all mother candidates, after the mating the father will
			//be reincorporated to the group but at the end.
			Genome father = p_subjects.remove(0);
			int rand_idx = (int) Math.random()*p_subjects.size();//not that now p_subjects size is reduced by 1.
			Genome mother = p_subjects.get(rand_idx);
			//now the mating
			double SOF = 1/father.getFitness()+1/mother.getFitness();//SOF stands for 'sumOfFitness'.
			ArrayList<Integer> nonAplicable = new ArrayList<>();//stores ctx genes already in genome
			for (int j = 0; j < g_size; j++) {
				Gen fatherGen = father.getGen(j);
				Gen motherGen = mother.getGen(j);
				Gen embryo = 
					new Gen(
						(int)((fatherGen.x/father.getFitness()+motherGen.x/mother.getFitness())/SOF),
						(int)((fatherGen.y/father.getFitness()+motherGen.y/mother.getFitness())/SOF)
					);
				//findClosest to embryo
				int closest = embryo.findClosest(ctx);
				if (!offsprings.get(offsprings.size()-1).exist(closest)) {
					offsprings.get(offsprings.size()-1).addGen(ctx.get(closest), closest);
				}else {
					//find closest not in genome
					do {
						nonAplicable.add(closest);
						closest = embryo.findClosestNot(ctx,nonAplicable);
					}while(offsprings.get(offsprings.size()-1).exist(closest));//makes sure gene is not repeated in Genome
					offsprings.get(offsprings.size()-1).addGen(ctx.get(closest), closest);
				}
			}
		}
		return offsprings;
	}
}
