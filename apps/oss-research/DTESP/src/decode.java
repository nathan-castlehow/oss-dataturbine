
public class decode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] s = new String[9];
		s[0] = "Snaulrlqjhjcqclzesxcfycnqjctsacds";
		s[1] = "urpAfcfnpjnaqtcahfcxAcakacamlmyhl"; 
		s[2] = "hlnepgSlasxJpjdejepceOysnhnhfkptf";
		s[3] = "lnkhntahnlccnprccsnthjcllqkdcjeso";
		s[4] = "auaakcdayfsakNGcZlkpaceyscdctjclj";
		s[5] = "pSnjShnkcPqyalchcfuashnpfJeHjphfh"; 
		s[6] = "nurnqaytSnqccaetcSseqqjhecplpYtjd"; 
		s[7] = "khlxjechlkurlesCojasscztaanzHhanc"; 
		s[8] = "alnsssqjetcszclwqnellJpyOykftrhx"; 

		for(int c=0; 0 < s[0].length(); c++){
			for(int r=0; r < 9; r++){
				System.out.print(s[r].charAt(c));
			}
			System.out.println();
		}
	}

}
