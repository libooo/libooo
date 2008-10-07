package com.okay.validate;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

public class ImageData {
	public int[][] data;
	public int w;
	public int h;
	public char code;

	public ImageData() {
	}

	public ImageData(BufferedImage bi) {
		this(bi, new WhiteFilter());
	}

	public ImageData(BufferedImage bi, Filter filter) {
		h = bi.getHeight();
		w = bi.getWidth();
		data = new int[h][w];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int p = bi.getRGB(j, i);
				data[i][j] = p;
			}
		}
		filter.doFilter(data);
	}

	public ImageData clone(int x, int y, int w0, int h0) {
		ImageData ia = new ImageData();
		try{
		ia.w = w0;
		ia.h = h0;
		ia.data = new int[ia.h][ia.w];
		for (int i = 0; i < h0; i++) {
			for (int j = 0; j < w0; j++) {
				ia.data[i][j] = data[i + y][j + x];
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return ia;
	}

	// 修正除噪
	public void modify() {
		for (int i = 0; i < h; i++) {
			if (i == 0 || i == h - 1) {
				for (int j = 0; j < w; j++) {
					data[i][j] = 1;
				}
			} else {
				for (int j = 0; j < w; j++) {
					if (j == 0 || j == w - 1) {
						data[i][j] = 1;
						continue;
					}
					if (true) {
						if (data[i][j] == 0
								&& data[i - 1][j - 1] + data[i][j - 1]
										+ data[i + 1][j - 1] + data[i - 1][j]
										+ data[i + 1][j] + data[i - 1][j + 1]
										+ data[i][j + 1] + data[i + 1][j + 1] > 5)
							data[i][j] = 1;
					}
				}
			}
		}
	}

	public void show() {
		System.out.println();
		for (int i = 1; i < h; i++) {
			for (int j = 1; j < w; j++) {
				System.out.print((data[i][j] == 1 ? "1" : " ") + "");
			}
			System.out.println();
		}
		System.out.println();
	}

	public ImageData getSub(int phiex, int tot) {
		int border = 15;
		int subWidth = (w - 2 * border) / tot;
		int lbound = border + phiex * subWidth - (phiex == (tot - 1) ? 1 : 0)
				* 5;
		int ubound = border + (phiex + 1) * subWidth
				+ (phiex == (tot - 1) ? 0 : 1) * 5;
		int posx = lbound;
		int posy = 0;
		int posx1 = ubound;
		int posy1 = h;
		System.out.println(posx + "|" + posy + "|" + posx1 + "|" + posy1 + "|"
				+ lbound + "|" + ubound);
		posx = getPosX(lbound, ubound, posx);
		posx1 = getPosX1(lbound, ubound, posx1);
		lbound = posx;
		ubound = posx1;
		posy = getPosY(lbound, ubound, posy);
		posy1 = getPosY1(lbound, ubound, posy1);
		System.out.println(posx + "|" + posy + "|" + posx1 + "|" + posy1 + "|"
				+ lbound + "|" + ubound);
		return clone(posx - 2, posy - 2, posx1 - posx + 3, posy1 - posy + 3);
	}

	public void cal() {
		int rec[] = new int[w];
		for (int x = 0; x < w; x++) {
			int s = 0;
			for (int y = 0; y < h; y++) {
				s = s + data[y][x];
			}
			// if(s==h)rec[x]=1;else rec[x]=0;//rec记录数组的断连情况
			rec[x] = s;
		}
		System.out.println("rec: ");
		for (int i = 0; i < w; i++) {
			System.out.print(rec[i] + ",");
		}
		System.out.println("");
		// for(int i=0,x=1;x<w-1;x++)if(rec[x-1]==0 && rec[x]==1 &&
		// rec[x+1]==1){i++;lf[i]=x;}//计算每个字符的左边界
		// for(int i=0,x=1;x<w-1;x++)if(rec[x-1]==1 && rec[x]==1 &&
		// rec[x+1]==0){i++;rt[i]=x;}//计算每个字符的右边界
		// for(int i=1;i<=N;i++)for(x=0;x<W;x++)for(y=0;y<H;y++) if(x>=lf[i] &&
		// x<=rt[i] && YZM[x][y]==1)YZM[x][y]=i;
		// for(int
		// y=H-1;y>=0;y--)for(x=0;x<W;x++)for(i=1;i<=N;i++)if(YZM[x][y]==i)up[i]=y;//计算每个字符的上边界
	}

	public int getPosX(int lbound, int ubound, int posx) {
		int _posx = posx;

		for (int x = lbound; x < ubound; x++) {
			int num = 0;
			for (int y = 0; y < h; y++) {
				num += data[y][x];
			}
			if (Math.abs(num - h) > 2) {
				return _posx;
			} else {
				_posx++;
			}
		}

		return posx;
	}

	public int getPosY(int lbound, int ubound, int posy) {
		int _posy = posy;

		for (int y = 0; y < h; y++) {
			String str = "";
			for (int x = lbound; x < ubound; x++) {
				str += data[y][x];
			}
			if (str.indexOf("00") != -1) {
				return _posy;
			} else {
				_posy++;
			}
		}
		return posy;
	}

	public int getPosX1(int lbound, int ubound, int posx) {
		int _posx = posx;
		for (int x = ubound - 1; x > lbound; x--) {
			int num = 0;
			for (int y = 0; y < h; y++) {
				num += data[y][x];
			}
			if (Math.abs(num - h) > 3) {
				return _posx;
			} else {
				_posx--;
			}
		}
		return posx;
	}

	public int getPosY1(int lbound, int ubound, int posy1) {
		int _posy = posy1;

		for (int y = h - 1; y >= 0; y--) {
			String str = "";
			for (int x = lbound; x < ubound; x++) {
				str += data[y][x];
			}
			if (str.indexOf("00") != -1) {
				return _posy;
			} else {
				_posy--;
			}
		}
		return posy1;
	}

	public int[] getSplitPointsX(int charNum, int minWidth) {
		int[] posx = new int[charNum * 2];

		try {
			StringBuffer _buf = new StringBuffer();
			for (int i = 0; i < w; i++) {
				int temp = 0;
				for (int j = 0; j < h; j++) {
					temp += data[j][i];
				}
				_buf.append(Math.abs(temp - h) <2 ? "#" : "@");
			}
			String sample = _buf.toString();
			String[] str = sample.split("#{5,}");
//			for (int i = 0; i < str.length; i++) {
//				System.out.println(str[i].length() + "|" + str[i]);
//			}

			int indexs = 0;
			int tempx = 0;
			for (int i = 0; i < str.length; i++) {
				if (str[i].length() > minWidth) {
					tempx = sample.indexOf("#" + str[i] + "#", tempx);
					posx[indexs] = tempx + 1;
					indexs++;
					posx[indexs] = posx[indexs - 1] + str[i].length();
					indexs++;
				}
			}
//			System.out.println("-------------");
//			for (int i = 0; i < posx.length; i++) {
//				System.out.print("\t" + posx[i]);
//			}
//			System.out.println("");
//			System.out.println("-------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return posx;
	}

	public int[] getSplitPointsY(int minWidth) {
		int[] posx = new int[2];

		try {
			StringBuffer _buf = new StringBuffer();
			for (int i = 0; i < h; i++) {
				int temp = 0;
				for (int j = 0; j < w; j++) {
					temp += data[i][j];
				}
				_buf.append(Math.abs(temp - w) ==0 ? "#" : "@");
			}
			String sample = _buf.toString();
			String[] str = sample.split("#+");
			for (int i = 0; i < str.length; i++) {
//				System.out.println(str[i].length() + "|" + str[i]);
			}
			int tempx = 0;
			for (int i = 0; i < str.length; i++) {
				if (str[i].length() > minWidth) {
					minWidth=str[i].length();
					tempx = sample.indexOf("#" + str[i] + "#", tempx);
					posx[0] = tempx + 1;
					posx[1] = posx[0] + str[i].length();
				}
			}
//			System.out.println("-------------");
//			for (int i = 0; i < posx.length; i++) {
//				System.out.print("\t" + posx[i]);
//			}
//			System.out.println("");
//			System.out.println("-------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return posx;
	}

	public ImageData[] split(int charNum, int minWidth) {
		ImageData[] sub = new ImageData[charNum];
		int[] splitPoints = getSplitPointsX(charNum, minWidth);
		for (int i = 0; i < charNum; i++) {
			int lbound = splitPoints[2 * i];
			int ubound = splitPoints[2 * i + 1];
			int posx = lbound;
			int posy = 0;
			int posx1 = ubound;
			int posy1 = h;
//			System.out.println(posx + "|" + posy + "|" + posx1 + "|" + posy1
//					+ "|" + lbound + "|" + ubound);
			
			ImageData subImg = this.clone(posx, posy, (posx1-posx), posy1);
			int posyy[] = subImg.getSplitPointsY(10);
			posy = posyy[0];
			posy1 = posyy[1];
//			System.out.println(posx + "|" + posy + "|" + posx1 + "|" + posy1
//					+ "|" + lbound + "|" + ubound);
//			sub[i] = clone(posx - 2, posy - 2, posx1 - posx + 3, posy1 - posy
//					+ 3);
			sub[i] = clone(posx , posy , posx1 - posx , posy1 - posy);
		}
		return sub;
	}


	public void writeImg(File imgName) throws IOException {
		try{
		BufferedImage img = new BufferedImage(this.w, this.h,
				BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int rgb = 0;
				if (data[j][i] == 0) {
					rgb = 0;
				} else {
					rgb = 0xffffff;
				}
				img.setRGB(i, j, rgb);
			}
		}
		ImageIO.write(img, "JPEG", imgName);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < h; j++) {
			sb.append("\n");
			for (int i = 0; i < w; i++) {
				sb.append(data[j][i]);
			}
		}
		return sb.toString();
	}

	public void writeData(File dataFile) throws FileNotFoundException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(dataFile);

			pw.write(this.toString());
		} finally {
			pw.close();
		}
	}
}
