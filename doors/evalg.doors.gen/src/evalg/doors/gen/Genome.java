package evalg.doors.gen;

import java.util.ArrayList;

public class Genome {
	/*
	 * Me di cuenta que estoy puro webiando con la clase Pair, osea,
	 * es una excelente estructura de datos que Java debiese implementar....
	 * pero lo que nescesito no es tan amplio, sino más bien específico,
	 * la voy a cambiar por una clase GEN que implemente un Par(int, int). 
	 * */
	private ArrayList<Pair<Integer,Integer>> gen_list;
	private ArrayList<Integer> gen_id_list;
	private int size;
	
	public Genome(int size) {
		this.size = size;
		this.gen_list = new ArrayList<>(size);
		this.gen_id_list = new ArrayList<>(size);
	}
	
	public Pair<Integer,Integer> getGen(int i){
		return gen_list.get(i);
	}
	
	public void addGen(Pair<Integer,Integer> gen, int gen_id) {
		if (this.gen_list.size() < this.size) {
			this.gen_list.add(gen);
			this.gen_id_list.add(gen_id);
		}else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	public boolean exist(int gen_id) {
		return this.gen_id_list.contains(gen_id);
	}
}
