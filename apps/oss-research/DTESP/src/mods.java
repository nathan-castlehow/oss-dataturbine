
public class mods {

	public static void main(String[] args) {
		
//		for(int i=2; i<25; i++){
//			if(i%5 == 0) continue;
//			findOrder(i, 25);
//		}
		
		for(int p=2; p<90; p++){
			if(!isPrime(p)) continue;
			
			for(int a=1; a<60; a++){
				if(!isOrder16(a,p)) break;
			}
		}
	}
	
	static boolean isPrime(int n) {
	    //check if n is a multiple of 2
	    if (n%2==0) return false;
	    //if not, then just check the odds
	    for(int i=3;i*i<=n;i+=2) {
	        if(n%i==0)
	            return false;
	    }
	    return true;
	}
	
	static int findOrder(int a, int modM){
		
		int r = a;
		int count =1;
		
		System.out.println(count+":\t"+r);
		while( r != 1){
			count++;
			System.out.print(count+":\t"+r +" * "+a+" = ");
			r = (r * a) % modM;
			System.out.println(r);
		}
		
		return count;
	}
	
	
	static boolean isOrder16(int a, int m){
		int r = ((int)Math.pow(a, 16)) % m;
		
		if(r!= 1){
			System.out.println(a+"^16 = "+r+"\t(mod "+m+")");
			return false;
		}
		
		return true;
	} 
}
