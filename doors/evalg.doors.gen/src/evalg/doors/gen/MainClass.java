package evalg.doors.gen;

import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.app.App;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.ArrayList;


public class MainClass {
	
	public static int getGenIndex(double r, int max) {
		/*
		 * Lo hago como funcion, para encapsular, y as� dejar m�s 
		 * limpio el codigo y hacerlo m�s escalable.
		 * */
		return (int) Math.floor(max*r);
	}
	
	public static ArrayList<Genome> runSimulation(HeadlessWorkspace wkspc, String buildingPath, String path, ArrayList<Genome> generation, int gen_idx) {
		ArrayList<Genome> best_worst = new ArrayList<>();
		int subject_idx = 0;
		for(Genome subject:generation) {
			String doorsFilePath = path+"generation_"+gen_idx+"/subj_"+subject_idx+"/doors.txt";
			try {
				PrintWriter doorsFile = new PrintWriter(doorsFilePath, "UTF-8");
				for (int i = 0; i < subject.getSize(); i++) {
					Gen gen = subject.getGen(i);
					doorsFile.println(gen.x+" "+gen.y);
				}
				doorsFile.close();
				//set arrays
				wkspc.command("set patch-data []");
				wkspc.command("set door-data []");
				//clear prev data
				wkspc.clearTurtles();
				wkspc.command("reset-ticks");
				//open building file
				wkspc.command("file-open "+buildingPath);
				//read building file
				wkspc.command("while [ not file-at-end? ] [ set patch-data sentence patch-data (list (list file-read file-read file-read)) ]");
				//close building file
				wkspc.command("file-close");
				//open doors file
				wkspc.command("file-open "+doorsFilePath);
				//read doors file
				wkspc.command("while [ not file-at-end? ] [ set door-data sentence door-data (list (list file-read file-read)) ]");
				//close doors file
				wkspc.command("file-close");
				//determinar poblacion
				//generar poblacion
				
				
				//correr sym -> implementar en netlogo
				subject.setFitness((Integer) wkspc.report("ticks"));
				if(best_worst.size() < 1) {
					//al comienzo el primer sujeto es el mejor y el peor
					best_worst.add(subject);
					best_worst.add(subject);
				}else{
					
				}
			}catch (UnsupportedEncodingException uee) {
				uee.printStackTrace();
			}catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
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
	
	public static long calcVariabilidad(ArrayList<Genome> generation) {
		return 0;
	}

	public static void main(String[] args) {
		String wkspc = new File("").getAbsolutePath()+"/src/";
		String [] buildingFiles = {"conference.plan","office.plan","office.plan"};
		int generation_size = 20;
		int genome_size = 5;
		
		HeadlessWorkspace NLworkspace = HeadlessWorkspace.newInstance();
		try {
            NLworkspace.open(wkspc+"program/escape4_v6.nlogo");
		
			for(String buildingFile: buildingFiles) {
				//la idea es: por cada archivo, parsear los puntos y ponerlos en contenedores distintos
				ArrayList<Gen> outside = new ArrayList<>();
				ArrayList<Gen> inside = new ArrayList<>();
				ArrayList<Gen> wall = new ArrayList<>();
				String buildingPath = wkspc+"buildings/"+buildingFile;
				File buildingPlan = new File(buildingPath);
				try {
					Scanner reader = new Scanner(buildingPlan);
					while(reader.hasNextLine()) {
						String [] line = reader.nextLine().split(" ");
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
					int gen_idx = 0;
					double var_delta = 0; //some number;
					do {
						runSimulation(NLworkspace, buildingPath, wkspc, generations.get(gen_idx), gen_idx);
						//select best
						//select worst
						//generate random
						//select for mutation
						//mutate
						//select for reproduction
						//reproduce
					}while(calcVariabilidad(generations.get(gen_idx++)) <= var_delta);
					
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
		return new ArrayList<Genome>();
	}
	
	public static ArrayList<Genome> selectForReproduction(ArrayList<Genome> generation, int k){
		//para este proceso se utilizar� la metodolog�a de torneo
		return new ArrayList<Genome>(); 
	}

}
