import java.awt.Color;
public class SeamCarver {
	private Picture p;
	private Picture newPic;
	private int width;
	private int height;
	private double[][] energyMatrix;
	private boolean isEnergyMatrixTransposed;
	private int[][] edgeTo;
	private double[] distTo;
	private int[] seam;
	//private int[] removedSeam;
	
	public SeamCarver(Picture picture) {
		
		p = new Picture(picture);
		width = picture.width();
		height = picture.height();
		energyMatrix = new double[width][height];
		isEnergyMatrixTransposed = false;
		edgeTo = new int[width][height];
		distTo = new double[width];
		
//		for (int i = 0; i < width; i++) {
//			distTo[i] = 195075.0;
//		}
		seam = new int[height];
		for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
                energy(i, j);
        }
	}
	
	public Picture picture() {
		return p;
	}
	
	public     int width() {
		return width;
	}
	
	public     int height() {
		return height;
	}
	
	public  double energy(int x, int y) {
		
		if (x == 0 || y == 0 || x == width-1 || y == height-1) {
			energyMatrix[x][y] = 195075.0;
			return 195075.0;
		}
		
		Color c1 = p.get(x+1, y);
		Color c2 = p.get(x-1, y);
		int dxsquare = (c1.getRed()-c2.getRed())*(c1.getRed()-c2.getRed()) + (c1.getGreen()-c2.getGreen())*(c1.getGreen()-c2.getGreen())
				+ (c1.getBlue()-c2.getBlue())*(c1.getBlue()-c2.getBlue());
		
		Color c3 = p.get(x, y+1);
		Color c4 = p.get(x, y-1);
		int dysquare = (c3.getRed()-c4.getRed())*(c3.getRed()-c4.getRed()) + (c3.getGreen()-c4.getGreen())*(c3.getGreen()-c4.getGreen())
				+ (c3.getBlue()-c4.getBlue())*(c3.getBlue()-c4.getBlue());
		
		energyMatrix[x][y] = dxsquare + dysquare;
		return dxsquare + dysquare;
	}

	public   int[] findVerticalSeam() {
		
		if (width <= 1)
			throw new IndexOutOfBoundsException();
		for (int i = 1; i < width-1; i++) {
			distTo[i] = 195075.0 + energyMatrix[i][1];
			//distTo[i][1] = energyMatrix[i][1];
			edgeTo[i][1] = i;
		}
		computeSeam();
		int[] copySeam = new int[seam.length];
		System.arraycopy(seam, 0, copySeam, 0, seam.length);
		
		if (isEnergyMatrixTransposed) {
			transposeEnergyMatrix();
			isEnergyMatrixTransposed = false;
		}
		
		return copySeam;
	}
	
	 public   int[] findHorizontalSeam() {
		 
		 if (height <= 1)
			 throw new IndexOutOfBoundsException();
		 
		 if (!isEnergyMatrixTransposed) {
			 transposeEnergyMatrix();
			 isEnergyMatrixTransposed = true;
		 }
		
		 int[] mySeam = findVerticalSeam();
		 //System.out.println("Seam size: "+mySeam.length+" Width: "+width+" Height: "+width);
		 return mySeam;
	 }
	
	private void computeSeam() {
		for (int i = 1; i < height-2; i++) {
			double[] temp = new double[width];
			for (int j = 1; j < width-1; j++) {
				relax(j, i, temp);
			}
			distTo = temp;
		}
		
		double minDist = Double.POSITIVE_INFINITY;
		int col = -1;
		for (int i = 1; i < width-1; i++) {
			//System.out.println(distTo[i]+" "+energyMatrix[i][height-2]);
			if (distTo[i] < minDist) {
				minDist = distTo[i];
				col = i;
			}
		}
	
		int next = col;
		seam[height-1] = next;
		for (int i = height-2; i > 0; i--) {
			seam[i] = next;
			next = edgeTo[next][i];
		}
		seam[0] = next;	
	}
	
	private void relax(int x, int y, double[] temp) {
		//StdOut.printf("Relaxing pixel: "+x+","+y+"\n");
		
		for(int i = x-1; i <= x+1; i++) {
			if (i > 0 && i < width-1) {
				double d = distTo[x] + energyMatrix[i][y+1];
				if ((temp[i] == 0) || (temp[i] > d)) {
					temp[i] = d;
					edgeTo[i][y+1] = x;
				}
			}
		}	
	}
	public void removeHorizontalSeam(int[] a) {
		
//		for (int i=0; i<a.length; i++)
//			System.out.println("row: "+i+", col: "+a[i]);
//		removeVerticalSeam(a);
//		isEnergyMatrixTransposed = false;
//		transposeEnergyMatrix();
		if (height <= 1) {
			throw new IllegalArgumentException();
		}
		
		if (a.length != width)
			throw new IllegalArgumentException();
		
		newPic = new Picture(width, height-1);
		//StdOut.printf("New image is "+(width-1)+" x "+height+"\n");
		
		for (int w = 0; w < width; w++) {
			
			for (int h = a[w]; h < height-1; h++) {
				if (h < 0 || h > height-1)
					throw new IndexOutOfBoundsException();
				p.set(w, h, p.get(w, h+1));
				energyMatrix[w][h] = energyMatrix[w][h+1];
			}
		}
		for (int w = 0; w < width; w++) {
			for (int h  =0; h < height-1; h++) {
				//StdOut.printf("Writing new pic: "+w+", "+h+"\n");
				newPic.set(w, h, p.get(w, h));
			}
		}
		//removedSeam = a;
		reInitialize();	
	}
	public void removeVerticalSeam(int[] a) {
		
		if (width <= 1) {
			throw new IllegalArgumentException();
		}
		
		if (a.length != height)
			throw new IllegalArgumentException();
		
		/*int prev = a[0];
		if (prev < 0 || prev > width-1)
			throw new IllegalArgumentException();
		
		for (int i = 1; i < a.length; i++) {
			int c = a[i];
			//System.out.println("Prev: "+prev+", Curr: "+c);
			if ((c > prev + 1) && (c < prev - 1))
				throw new IllegalArgumentException();
			if (c < 0 || c > width-1)
				throw new IllegalArgumentException();
			prev = c;
		}*/
		
		newPic = new Picture(width-1, height);
		//StdOut.printf("New image is "+(width-1)+" x "+height+"\n");
		
		for (int h = 0; h < height; h++) {
			
			for (int w = a[h]; w < width-1; w++) {
				if (w < 0)
					throw new IndexOutOfBoundsException();
				p.set(w, h, p.get(w+1, h));
				energyMatrix[w][h] = energyMatrix[w+1][h];
			}
		}
		//System.out.println("removed seam");
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width-1; w++) {
				//StdOut.printf("Writing new pic: "+w+", "+h+"\n");
				newPic.set(w, h, p.get(w, h));
			}
		}
		//removedSeam = a;
		reInitialize();		
	}
	
	private void reInitialize() {
		
		p = newPic;
		newPic = null;
		width = p.width();
		height = p.height();
		double[][] tempMatrix = new double[width][height];
		
		for (int h = 0; h < height; h++)
			for (int w = 0; w < width; w++)
				tempMatrix[w][h] = energyMatrix[w][h];
		energyMatrix = tempMatrix;
		//energyMatrix = new double[width][height];
		edgeTo = new int[width][height];
		distTo = new double[width];
		for (int i = 0; i < width; i++) {
				distTo[i] =  195075.0;
		}
		seam = new int[height];

	}
	
	private void transposeEnergyMatrix() {
		int newHeight = width;
		int newWidth = height;
		double[][] newEnergy = new double[newWidth][newHeight];
		double[] newDistTo = new double[newWidth];
		for (int i = 0; i < newHeight; ++i) {
			for (int j = 0; j < newWidth; ++j) {
				newEnergy[j][i] = energyMatrix[i][j];
				//newDistTo[j][i] = distTo[i][j];
			}
		 }
		
		height = newHeight;
		width = newWidth;
		//energyMatrix = new double[width][height];
		energyMatrix = newEnergy;
		//distTo = new double[width][height];
		distTo = newDistTo;
		edgeTo = new int[width][height];
		seam = new int[height];
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Picture pic = new Picture("C:\\Users\\Kartik\\Google Drive\\SeamCarver\\HJoceanSmall.png");
		SeamCarver sc = new SeamCarver(pic);
		//pic.show();
		System.out.printf("Printing energy calculated for each pixel.\n");        

        for (int j = 0; j < sc.height(); j++)
        {
            for (int i = 0; i < sc.width(); i++)
                System.out.printf("%9.0f ", sc.energy(i, j));

            System.out.println();
        }
		int[] seam = sc.findVerticalSeam();
		for (int i: seam) {
			StdOut.printf(" "+i);
		}

	}
	
	

}
