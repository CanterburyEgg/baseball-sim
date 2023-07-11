import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.time.LocalDate;

public class GameGenerator 
{
	public static PrintWriter gameOutput = null, gameDebug = null;
	
	public static void main(String[] args) throws FileNotFoundException
	{
		boolean game = true;
		boolean base1 = false;
		int[] b1player = { -1, -1 };
		boolean base2 = false;
		int[] b2player = { -1, -1 };
		boolean base3 = false;
		int[] b3player = { -1, -1 };
		boolean extras = false;
		int runs1 = 0;
		int runs2 = 0;
		int hits1 = 0;
		int hits2 = 0;
		int outs = 0;
		int inning = 1;
		int rng = 0;
		
		int[][] team1 = new int[9][9]; //AB|R|H|2B|3B|HR|RBI|BB|SO
		int[][] team2 = new int[9][9];
		
		int[][] pitchers1 = new int[6][13]; //W|L|G|GS|SV|SVO|OUT|H|R|HR|BB|SO|BF
		int[][] pitchers2 = new int[6][13];
		
		pitchers1[0][3] = 1;
		pitchers2[0][3] = 1;
		
		int t1index = 0;
		int t2index = 0;
		
		int p1index = 0;
		int p2index = 0;
		
		String n1 = "", t1 = "", n2 = "", t2 = "";
		if (args.length == 0)
		{
			Scanner input = new Scanner(System.in);
			System.out.print("Away team: ");
			n1 = input.nextLine();
			System.out.print("Home team: ");
			n2 = input.nextLine();
			System.out.println();
			input.close();
		}
		else
		{
			n1 = args[0];
			n2 = args[1];
		}

		t1 = "Teams/" + n1 + ".txt";
		t2 = "Teams/" + n2 + ".txt";
		
		try {
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			Date date = new Date();
			gameOutput = new PrintWriter("Games/" + n1 + "_at_" + n2 + " " + df.format(date) + ".txt");
			gameDebug = new PrintWriter("Games/Debug Files/" + n1 + "_at_" + n2 + " " + df.format(date) + "_DEBUG.txt");
		} catch (IOException e)
		{
			System.out.println("Couldn't create game file.");
			e.printStackTrace();
			System.exit(0);
		}
		
		Scanner file1 = new Scanner(new File(t1));
		Scanner file2 = new Scanner(new File(t2));
		
		String[] team1batters = new String[9];
		String[] team2batters = new String[9];
		
		int[] t1contactranks = new int[9];
		int[] t2contactranks = new int[9];
		
		int[] t1powerranks = new int[9];
		int[] t2powerranks = new int[9];
		
		int[] t1controlranks = new int[6];
		int[] t2controlranks = new int[6];
		
		int[] t1stuffranks = new int[6];
		int[] t2stuffranks = new int[6];
		
		String[] team1pitchers = new String[6];
		String[] team2pitchers = new String[6];
		
		int p1 = -1;
		for (int i = 0; i < 15; i++)
		{
			if (i < 9)
			{
				team1batters[i] = file1.next() + " " + file1.next();
				t1contactranks[i] = file1.nextInt();
				t1powerranks[i] = file1.nextInt();
			}
			else
			{
				team1pitchers[i-9] = file1.next() + " " + file1.next();
				t1controlranks[i-9] = file1.nextInt();
				t1stuffranks[i-9] = file1.nextInt();
			}
		}
		
		int p2 = -1;
		for (int i = 0; i < 15; i++)
		{
			if (i < 9)
			{
				team2batters[i] = file2.next() + " " + file2.next();
				t2contactranks[i] = file2.nextInt();
				t2powerranks[i] = file2.nextInt();
			}
			else
			{
				team2pitchers[i-9] = file2.next() + " " + file2.next();
				t2controlranks[i-9] = file2.nextInt();
				t2stuffranks[i-9] = file2.nextInt();
			}
		}
		
		FullPrintLine("TOP INNING 1\n");
		while (game)
		{
			if (inning % 2 == 1)
			{
				FullPrint(team1batters[t1index] + ": ");
			}
			else
			{
				FullPrint(team2batters[t2index] + ": ");
			}
			
			rng = (int)(Math.random()*100)+1;
			
			if (inning % 2 == 1)
			{
				System.out.print("rng1 = " + rng + " + " + t1contactranks[t1index] + " - " + t2controlranks[p2index] + " - 1");
				gameDebug.print("rng1 = " + rng + " + " + t1contactranks[t1index] + " - " + t2controlranks[p2index] + " - 1");
				rng += t1contactranks[t1index];
				rng -= t2controlranks[p2index];
			}
			else
			{
				System.out.print("rng1 = " + rng + " + " + t2contactranks[t2index] + " - " + t1controlranks[p1index] + " + 1");
				gameDebug.print("rng1 = " + rng + " + " + t2contactranks[t2index] + " - " + t1controlranks[p1index] + " + 1");
				rng += t2contactranks[t2index];
				rng -= t1controlranks[p1index];
			}
			System.out.println(" = " + rng);
			gameDebug.println(" = " + rng);
			
			if (rng <= 10 || (rng >= 16 && rng <= 27))
			{
				FullPrintLine("Strikeout");
				
				if (inning % 2 == 1)
				{
					pitchers2[p2index][11]++;
					pitchers2[p2index][6]++;

					team1[t1index][8]++;
				}
				else
				{
					pitchers1[p1index][11]++;
					pitchers1[p1index][6]++;

					team2[t2index][8]++;
				}
				
				outs++;	
			}

			else if ((rng >= 69 && rng <= 71) || (rng >= 87 && rng <= 90))
			{
				FullPrintLine("Walk");
				
				if (inning % 2 == 1)
				{
					pitchers2[p2index][10]++;

					team1[t1index][0]--;
					team1[t1index][7]++;
				}
				else
				{
					pitchers1[p1index][10]++;

					team2[t2index][0]--;
					team2[t2index][7]++;
				}

				if (!base1 && !base2 && !base3)
				{
					base1 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && !base2 && !base3)
				{
					base1 = true;
					base2 = true;
					
					if (inning % 2 == 1)
					{
						b2player = b1player;
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b2player = b1player;
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (!base1 && base2 && !base3)
				{
					base1 = true;
					base2 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (!base1 && !base2 && base3)
				{
					base1 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && base2 && !base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && !base2 && base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b2player = b1player;
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b2player = b1player;
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (!base1 && base2 && base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && base2 && base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

						team1[t1index][6]++;

						team1[b3player[0]][1]++;
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t1index, p2index };

						runs1++;
					}
					else
					{
						pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

						team2[t2index][6]++;

						team2[b3player[0]][1]++;
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t2index, p1index };

						runs2++;
					}
				}
			}

			else if (rng == 86)
			{
				FullPrintLine("Hit by pitch");
				
				if (inning % 2 == 1)
				{
					team1[t1index][0]--;
				}
				else
				{
					team2[t2index][0]--;
				}
				
				if (!base1 && !base2 && !base3)
				{
					base1 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && !base2 && !base3)
				{
					base1 = true;
					base2 = true;
					
					if (inning % 2 == 1)
					{
						b2player = b1player;
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b2player = b1player;
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (!base1 && base2 && !base3)
				{
					base1 = true;
					base2 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (!base1 && !base2 && base3)
				{
					base1 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && base2 && !base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && !base2 && base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b2player = b1player;
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b2player = b1player;
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (!base1 && base2 && base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						b1player = new int[] { t1index, p2index };
					}
					else
					{
						b1player = new int[] { t2index, p1index };
					}
				}
				else if (base1 && base2 && base3)
				{
					base1 = true;
					base2 = true;
					base3 = true;
					
					if (inning % 2 == 1)
					{
						pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

						team1[t1index][6]++;

						team1[b3player[0]][1]++;
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t1index, p2index };

						runs1++;
					}
					else
					{
						pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

						team2[t2index][6]++;

						team2[b3player[0]][1]++;
						b3player = b2player;
						b2player = b1player;
						b1player = new int[] { t2index, p1index };

						runs2++;
					}
				}
			}
			else if ((rng >= 11 && rng <= 15) || (rng >= 28 && rng <= 68))
			{
				//contact (out)
				rng = (int)(Math.random()*100)+1;
				
				if (inning % 2 == 1)
				{
					System.out.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
					gameDebug.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
					rng += t1powerranks[t1index];
					rng -= t2stuffranks[p2index];
				}
				else
				{
					System.out.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
					gameDebug.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
					rng += t2powerranks[t2index];
					rng -= t1stuffranks[p1index];
				}
				System.out.println(" = " + rng);
				gameDebug.println(" = " + rng);
				
				if (rng <= 47)
				{
					FullPrintLine("Groundout");
					
					if ((!base1 && !base2 && !base3) || outs == 2)
					{
						outs++;
						
						if (inning % 2 == 1)
							pitchers2[p2index][6]++;
						else
							pitchers1[p1index][6]++;
					}
					else if (base1 && !base2 && !base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base1 = false;
							outs = outs + 2;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6] = pitchers2[p2index][6] + 2;
							else
								pitchers1[p1index][6] = pitchers1[p1index][6] + 2;
						}
						else if (rng == 1)
						{
							base1 = false;
							base2 = true;
							b2player = b1player;
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
						}
						else
						{
							if (inning % 2 == 1)
							{
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								b1player = new int[] { t2index, p1index };
							}
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
						}
					}
					else if (!base1 && base2 && !base3)
					{
						rng = (int)(Math.random()*2);
						if (rng == 0)
						{
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
						}
						else
						{
							base2 = false;
							base3 = true;
							b3player = b2player;
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
						}
					}
					else if (!base1 && !base2 && base3)
					{
						rng = (int)(Math.random()*2);
						if (rng == 0)
						{
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
						}
						else
						{
							base3 = false;
							outs++;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[p1index][6]++;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}
						}
					}
					else if (base1 && base2 && !base3)
					{
						rng = (int)(Math.random()*4);
						if (rng == 0)
						{
							outs++;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;

								b2player = b1player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								pitchers1[p1index][6]++;

								b2player = b1player;
								b1player = new int[] { t2index, p1index };
							}
						}
						else if (rng == 1)
						{
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
							
							base1 = false;
							base2 = true;
							base3 = true;
							
							b3player = b2player;
							b2player = b1player;
						}
						else if (rng == 2)
						{
							outs++;
														
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;

								b3player = b2player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								pitchers1[p1index][6]++;

								b3player = b2player;
								b1player = new int[] { t2index, p1index };
							}
						}
						else
						{
							base1 = false;
							base2 = false;
							base3 = true;
							
							outs = outs + 2;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6] = pitchers2[p2index][6] + 2;
							else
								pitchers1[p1index][6] = pitchers1[p1index][6] + 2;
							
							b3player = b2player;
						}
					}
					else if (base1 && !base2 && base3)
					{
						rng = (int)(Math.random()*4);
						if (rng == 0)
						{
							outs++;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;

								b1player = new int[] { t1index, p2index };
							}
							else
							{
								pitchers1[p1index][6]++;

								b1player = new int[] { t2index, p1index };
							}
						}
						else if (rng == 1)
						{
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
							
							base1 = false;
							base2 = true;
							
							b2player = b1player;
						}
						else if (rng == 2)
						{
							outs++;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[p1index][6]++;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}
							
							base1 = false;
							base2 = true;
							base3 = false;
							
							b2player = b1player;								
						}
						else
						{
							base1 = false;
							base2 = false;
							base3 = false;
							
							outs = outs + 2;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6] = pitchers2[p2index][6] + 2;
							else
								pitchers1[p1index][6] = pitchers1[p1index][6] + 2;
							
							if (outs < 3)
							{
								if (inning % 2 == 1)
								{
									pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

									team1[b3player[0]][1]++;
									team1[t1index][6]++;

									runs1++;
								}
								else
								{
									pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

									team2[b3player[0]][1]++;
									team2[t2index][6]++;

									runs2++;
								}	
							}
						}
					}
					else if (!base1 && base2 && base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							outs++;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6]++;
							else
								pitchers1[p1index][6]++;
						}
						else if (rng == 1)
						{
							outs++;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[p1index][6]++;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}
							
							base3 = false;
						}
						else
						{
							outs++;
							
							base1 = false;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;
								
								runs1++;
							}
							else
							{
								pitchers1[p1index][6]++;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}	

							b3player = b2player;
						}
					}
					else if (base1 && base2 && base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							outs++;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;

								b3player = b2player;
								b2player = b1player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								pitchers1[p1index][6]++;

								b3player = b2player;
								b2player = b1player;
								b1player = new int[] { t2index, p1index };							
							}
						}
						else if (rng == 1)
						{
							outs++;
							
							base1 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[p2index][6]++;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[p1index][6]++;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}
						
							b3player = b2player;
							b2player = b1player;
						}
						else
						{
							outs = outs + 2;
							
							if (inning % 2 == 1)
								pitchers2[p2index][6] = pitchers2[p2index][6] + 2;
							else
								pitchers1[p1index][6] = pitchers1[p1index][6] + 2;
							
							base1 = false;
							base2 = false;
							base3 = true;
							
							if (outs < 3)
							{
								if (inning % 2 == 1)
								{
									pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

									team1[b3player[0]][1]++;
									team1[t1index][6]++;

									runs1++;
								}
								else
								{
									pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

									team2[b3player[0]][1]++;
									team2[t2index][6]++;

									runs2++;
								}	
								
								b3player = b2player;
							}
						}
					}
				}
				else if (rng >= 48 && rng <= 58)
				{
					FullPrintLine("Lineout");
					outs++;
					
					if (inning % 2 == 1)
						pitchers2[p2index][6]++;
					else
						pitchers1[p1index][6]++;
				}
				else if (rng >= 59)
				{
					FullPrintLine("Flyout");
					outs++;
					
					if (inning % 2 == 1)
						pitchers2[p2index][6]++;
					else
						pitchers1[p1index][6]++;
					
					if (base2 && base3 && outs != 3)
					{
						rng = (int)(Math.random()*4);
						if (rng == 0 || rng == 1)
						{
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}		
						}
						else if (rng == 2)
						{
							base2 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}
							b3player = b2player;								
						}
					}
					else if (base3 && outs != 3)
					{
						rng = (int)(Math.random()*4);
						if (rng != 0)
						{
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;

								runs2++;
							}	
						}
					}					
					else if (base2 && outs != 3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base2 = false;
							base3 = true;
							b3player = b2player;
						}
					}
				}
				else
				{
					System.out.println("Error on number: " + rng);
					System.exit(0);
				}
			}
			else if ((rng >= 72 && rng <= 85) || (rng >= 91))
			{
				//contact (hit)
				rng = (int)(Math.random()*100)+1;
				
				if (inning % 2 == 1)
				{
					System.out.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
					gameDebug.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
					rng += t1powerranks[t1index];
					rng -= t2stuffranks[p2index];
				}
				else
				{
					System.out.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
					gameDebug.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
					rng += t2powerranks[t2index];
					rng -= t1stuffranks[p1index];
				}
				System.out.println(" = " + rng);
				gameDebug.println(" = " + rng);	
				
				if (rng <= 64)
				{
					FullPrintLine("Single");
					
					if (inning % 2 == 1)
					{
						pitchers2[p2index][7]++;

						team1[t1index][2]++;
						hits1++;
					}
					else
					{
						pitchers1[p1index][7]++;

						team2[t2index][2]++;
						hits2++;
					}	
					
					if (!base1 && !base2 && !base3)
					{
						base1 = true;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
							b1player = new int[] { t1index, p2index };
						else
							b1player = new int[] { t2index, p1index };
					}
					else if (base1 && !base2 && !base3)
					{
						rng = (int)(Math.random()*2);
						if (rng == 0)
						{
							base1 = true;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								b2player = b1player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								b2player = b1player;
								b1player = new int[] { t2index, p1index };
							}
						}
						else
						{
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								b3player = b1player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								b3player = b1player;
								b1player = new int[] { t2index, p1index };
							}
						}
					}
					else if (!base1 && base2 && !base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								b3player = b2player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								b3player = b2player;
								b1player = new int[] { t2index, p1index };
							}
						}
						else
						{
							base1 = true;
							base2 = false;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[t1index][6]++;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[t2index][6]++;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}	
						}
					}
					else if (!base1 && !base2 && base3)
					{
						base1 = true;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b3player[0]][1]++;
							team1[t1index][6]++;
							b1player = new int[] { t1index, p2index };

							runs1++;
						}
						else
						{
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b3player[0]][1]++;
							team2[t2index][6]++;
							b1player = new int[] { t2index, p1index };

							runs2++;
						}
					}
					else if (base1 && base2 && !base3)
					{
						rng = (int)(Math.random()*5);
						if (rng == 0)
						{
							base1 = true;
							base2 = true;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								b3player = b2player;
								b2player = b1player;
								b1player = new int[] { t1index, p2index };
							}
							else
							{
								b3player = b2player;
								b2player = b1player;
								b1player = new int[] { t2index, p1index };
							}
						}
						else if (rng == 1 || rng == 2)
						{
							base1 = true;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[t1index][6]++;
								b2player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[t2index][6]++;
								b2player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}
						}
						else
						{
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[t1index][6]++;
								b3player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[t2index][6]++;
								b3player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}		
						}
					}
					else if (base1 && !base2 && base3)
					{
						rng = (int)(Math.random()*2);
						if (rng == 0)
						{
							base1 = true;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;
								b2player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;
								b2player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}				
						}
						else
						{
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;
								b3player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;
								b3player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}	
						}
					}
					else if (!base1 && base2 && base3)
					{
						rng = (int)(Math.random()*2);
						if (rng == 0)
						{
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;
								b3player = b2player;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;
								b3player = b2player;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}				
						}
						else
						{
							base1 = true;
							base2 = false;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[b3player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 2;
								b1player = new int[] { t1index, p2index };

								runs1 = runs1 + 2;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[b3player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 2;
								b1player = new int[] { t2index, p1index };

								runs2 = runs2 + 2;
							}	
						}
					}
					else if (base1 && base2 && base3)
					{
						rng = (int)(Math.random()*5);
						if (rng == 0)
						{
							base1 = true;
							base2 = true;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;
								b3player = b2player;
								b2player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;
								b3player = b2player;
								b2player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2++;
							}				
						}
						else if (rng == 1 || rng == 2)
						{
							base1 = true;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[b3player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 2;
								b2player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1 = runs1 + 2;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[b3player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 2;
								b2player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2 = runs2 + 2;
							}
						}
						else
						{
							base1 = true;
							base2 = false;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[b3player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 2;
								b3player = b1player;
								b1player = new int[] { t1index, p2index };

								runs1 = runs1 + 2;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[b3player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 2;
								b3player = b1player;
								b1player = new int[] { t2index, p1index };

								runs2 = runs2 + 2;
							}
						}
					}
				}
				
				else if ((rng >= 65 && rng <= 77) || (rng >= 86 && rng <= 92))
				{
					FullPrintLine("Double");
					
					if (inning % 2 == 1)
					{
						pitchers2[p2index][7]++;

						team1[t1index][2]++;
						team1[t1index][3]++;

						hits1++;
					}
					else
					{
						pitchers1[p1index][7]++;

						team2[t2index][2]++;
						team2[t2index][3]++;

						hits2++;
					}	
					
					if (!base1 && !base2 && !base3)
					{
						base1 = false;
						base2 = true;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							b2player = new int[] { t1index, p2index };
						}
						else
						{
							b2player = new int[] { t2index, p1index };
						}
					}
					else if (base1 && !base2 && !base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base1 = false;
							base2 = true;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								b3player = b1player;
								b2player = new int[] { t1index, p2index };
							}
							else
							{
								b3player = b1player;
								b2player = new int[] { t2index, p1index };
							}
						}
						else
						{
							base1 = false;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;

								team1[b1player[0]][1]++;
								team1[t1index][6]++;
								b2player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;

								team2[b1player[0]][1]++;
								team2[t2index][6]++;
								b2player = new int[] { t2index, p1index };

								runs2++;
							}	
						}
					}
					else if (!base1 && base2 && !base3)
					{
						base1 = false;
						base2 = true;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

							team1[b2player[0]][1]++;
							team1[t1index][6]++;
							b2player = new int[] { t1index, p2index };

							runs1++;
						}
						else
						{
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

							team2[b2player[0]][1]++;
							team2[t2index][6]++;
							b2player = new int[] { t2index, p1index };

							runs2++;
						}			
					}
					else if (!base1 && !base2 && base3)
					{
						base1 = false;
						base2 = true;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b3player[0]][1]++;
							team1[t1index][6]++;
							b2player = new int[] { t1index, p2index };

							runs1++;
						}
						else
						{
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b3player[0]][1]++;
							team2[t2index][6]++;
							b2player = new int[] { t2index, p1index };

							runs2++;
						}		
					}
					else if (base1 && base2 && !base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base1 = false;
							base2 = true;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[t1index][6]++;
								b3player = b1player;
								b2player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[t2index][6]++;
								b3player = b1player;
								b2player = new int[] { t2index, p1index };

								runs2++;
							}	
						}
						else
						{
							base1 = false;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

								team1[b1player[0]][1]++;
								team1[b2player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 2;
								b2player = new int[] { t1index, p2index };

								runs1 = runs1 + 2;
							}
							else
							{
								pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

								team2[b1player[0]][1]++;
								team2[b2player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 2;
								b2player = new int[] { t2index, p1index };

								runs2 = runs2 + 2;
							}	
						}
					}
					else if (base1 && !base2 && base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base1 = false;
							base2 = true;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b3player[0]][1]++;
								team1[t1index][6]++;
								b3player = b1player;
								b2player = new int[] { t1index, p2index };

								runs1++;
							}
							else
							{
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b3player[0]][1]++;
								team2[t2index][6]++;
								b3player = b1player;
								b2player = new int[] { t2index, p1index };

								runs2++;
							}
						}
						else
						{
							base1 = false;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b1player[0]][1]++;
								team1[b3player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 2;
								b2player = new int[] { t1index, p2index };

								runs1 = runs1 + 2;
							}
							else
							{
								pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b1player[0]][1]++;
								team2[b3player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 2;
								b2player = new int[] { t2index, p1index };

								runs2 = runs2 + 2;
							}	
						}
					}
					else if (!base1 && base2 && base3)
					{
						base1 = false;
						base2 = true;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b2player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;
							b2player = new int[] { t1index, p2index };

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b2player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;
							b2player = new int[] { t2index, p1index };

							runs2 = runs2 + 2;
						}	
					}
					else if (base1 && base2 && base3)
					{
						rng = (int)(Math.random()*3);
						if (rng == 0)
						{
							base1 = false;
							base2 = true;
							base3 = true;
							
							if (inning % 2 == 1)
							{
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b2player[0]][1]++;
								team1[b3player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 2;
								b3player = b1player;
								b2player = new int[] { t1index, p2index };

								runs1 = runs1 + 2;
							}
							else
							{
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b2player[0]][1]++;
								team2[b3player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 2;
								b3player = b1player;
								b2player = new int[] { t2index, p1index };

								runs2 = runs2 + 2;
							}		
						}
						else
						{
							base1 = false;
							base2 = true;
							base3 = false;
							
							if (inning % 2 == 1)
							{
								pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
								pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
								pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

								team1[b1player[0]][1]++;
								team1[b2player[0]][1]++;
								team1[b3player[0]][1]++;
								team1[t1index][6] = team1[t1index][6] + 3;
								b2player = new int[] { t1index, p2index };

								runs1 = runs1 + 3;
							}
							else
							{
								pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
								pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
								pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

								team2[b1player[0]][1]++;
								team2[b2player[0]][1]++;
								team2[b3player[0]][1]++;
								team2[t2index][6] = team2[t2index][6] + 3;
								b2player = new int[] { t2index, p1index };

								runs2 = runs2 + 3;
							}
						}
					}
				}
				else if ((rng >= 79 && rng <= 85) || rng >= 94)
				{
					FullPrintLine("Home run");
					
					if (inning % 2 == 1)
					{
						pitchers2[p2index][7]++;
						pitchers2[p2index][9]++;

						team1[t1index][2]++;
						team1[t1index][5]++;

						hits1++;
					}
					else
					{
						pitchers1[p1index][7]++;
						pitchers1[p1index][9]++;

						team2[t2index][2]++;
						team2[t2index][5]++;

						hits2++;
					}
					
					if (!base1 && !base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;

							team1[t1index][1]++;
							team1[t1index][6]++;

							runs1++;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;

							team2[t2index][1]++;
							team2[t2index][6]++;

							runs2++;
						}
					}
					else if (base1 && !base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;

							runs2 = runs2 + 2;
						}
					}
					else if (!base1 && base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

							team1[b2player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

							team2[b2player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;

							runs2 = runs2 + 2;
						}
					}
					else if (!base1 && !base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b3player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b3player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;

							runs2 = runs2 + 2;
						}	
					}
					else if (base1 && base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[b2player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 3;

							runs1 = runs1 + 3;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[b2player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 3;

							runs2 = runs2 + 3;
						}	
					}
					else if (base1 && !base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 3;

							runs1 = runs1 + 3;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 3;

							runs2 = runs2 + 3;
						}	
					}
					else if (!base1 && base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b2player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 3;

							runs1 = runs1 + 3;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b2player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 3;

							runs2 = runs2 + 3;
						}	
					}
					else if (base1 && base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = false;
						
						if (inning % 2 == 1)
						{
							pitchers2[p2index][8] = pitchers2[p2index][8] + 1;
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[b2player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][1]++;
							team1[t1index][6] = team1[t1index][6] + 4;

							runs1 = runs1 + 4;
						}
						else
						{
							pitchers1[p1index][8] = pitchers1[p1index][8] + 1;
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[b2player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][1]++;
							team2[t2index][6] = team2[t2index][6] + 4;

							runs2 = runs2 + 4;
						}	
					}
				}
				else if (rng == 78 || rng == 93)
				{
					FullPrintLine("Triple");
					
					if (inning % 2 == 1)
					{
						pitchers2[p2index][7]++;

						team1[t1index][2]++;
						team1[t1index][4]++;

						hits1++;
					}
					else
					{
						pitchers1[p1index][7]++;

						team2[t2index][2]++;
						team2[t2index][4]++;

						hits2++;
					}	
					
					if (!base1 && !base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							b3player = new int[] { t1index, p2index };
						}
						else
						{
							b3player = new int[] { t2index, p1index };
						}
					}
					else if (base1 && !base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[t1index][6]++;
							b3player = new int[] { t1index, p2index };

							runs1++;
						}
						else
						{
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[t2index][6]++;
							b3player = new int[] { t2index, p1index };

							runs2++;
						}		
					}
					else if (!base1 && base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

							team1[b2player[0]][1]++;
							team1[t1index][6]++;
							b3player = new int[] { t1index, p2index };

							runs1++;
						}
						else
						{
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

							team2[b2player[0]][1]++;
							team2[t2index][6]++;
							b3player = new int[] { t2index, p1index };

							runs2++;
						}		
					}
					else if (!base1 && !base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b3player[0]][1]++;
							team1[t1index][6]++;
							b3player = new int[] { t1index, p2index };

							runs1++;
						}
						else
						{
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b3player[0]][1]++;
							team2[t2index][6]++;
							b3player = new int[] { t2index, p1index };

							runs2++;
						}
					}
					else if (base1 && base2 && !base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[b2player[0]][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;
							b3player = new int[] { t1index, p2index };

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[b2player[0]][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;
							b3player = new int[] { t2index, p1index };

							runs2 = runs2 + 2;
						}	
					}
					else if (base1 && !base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;
							b3player = new int[] { t1index, p2index };

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;
							b3player = new int[] { t2index, p1index };

							runs2 = runs2 + 2;
						}		
					}
					else if (!base1 && base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b2player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][6] = team1[t1index][6] + 2;
							b3player = new int[] { t1index, p2index };

							runs1 = runs1 + 2;
						}
						else
						{
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b2player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][6] = team2[t2index][6] + 2;
							b3player = new int[] { t2index, p1index };

							runs2 = runs2 + 2;
						}
					}
					else if (base1 && base2 && base3)
					{
						base1 = false;
						base2 = false;
						base3 = true;
						
						if (inning % 2 == 1)
						{
							pitchers2[b1player[1]][8] = pitchers2[b1player[1]][8] + 1;
							pitchers2[b2player[1]][8] = pitchers2[b2player[1]][8] + 1;
							pitchers2[b3player[1]][8] = pitchers2[b3player[1]][8] + 1;

							team1[b1player[0]][1]++;
							team1[b2player[0]][1]++;
							team1[b3player[0]][1]++;
							team1[t1index][6] = team1[t1index][6] + 3;
							b3player = new int[] { t1index, p2index };

							runs1 = runs1 + 3;
						}
						else
						{
							pitchers1[b1player[1]][8] = pitchers1[b1player[1]][8] + 1;
							pitchers1[b2player[1]][8] = pitchers1[b2player[1]][8] + 1;
							pitchers1[b3player[1]][8] = pitchers1[b3player[1]][8] + 1;

							team2[b1player[0]][1]++;
							team2[b2player[0]][1]++;
							team2[b3player[0]][1]++;
							team2[t2index][6] = team2[t2index][6] + 3;
							b3player = new int[] { t2index, p1index };

							runs2 = runs2 + 3;
						}			
					}
				}
				else
				{
					System.out.println("Error on number: " + rng);
					System.exit(0);
				}
			}
			else
			{
				System.out.println("Error on number: " + rng);
				System.exit(0);
			}
			
//-------------------------------------------------------------------------------------------------------------------------------------------
			
			if (inning % 2 == 1)
			{
				team1[t1index][0]++;
				t1index++;
				if (t1index == 9)
					t1index = 0;
			}
			else
			{
				team2[t2index][0]++;
				t2index++;
				if (t2index == 9)
					t2index = 0;
			}
			
			if (inning % 2 == 1)
				pitchers2[p2index][12]++;
			else
				pitchers1[p1index][12]++;
			
			if (inning % 2 == 0)
				gameDebug.println("Team 1 pitcher (" + team1pitchers[p1index] + "): " + pitchers1[p1index][12] + " batters faced, " + pitchers1[p1index][8] + " runs allowed = " + pitchers1[p1index][12] + " + " + 2.5*(pitchers1[p1index][8]) + " = " + (pitchers1[p1index][12] + 2.5*(pitchers1[p1index][8]) + ", outs: " + outs));
			else
				gameDebug.println("Team 2 pitcher (" + team2pitchers[p2index] + "): " + pitchers2[p2index][12] + " batters faced, " + pitchers2[p2index][8] + " runs allowed = " + pitchers2[p2index][12] + " + " + 2.5*(pitchers2[p2index][8]) + " = " + (pitchers2[p2index][12] + 2.5*(pitchers2[p2index][8]) + ", outs: " + outs));
			gameDebug.println("");
			
			if (outs < 3)
			{
				String baseDebug = "";

				if (base1)
					baseDebug = "base1: [" + b1player[0] + "," + b1player[1] + "], ";
				else
					baseDebug = "base1: false, ";
				if (base2)
					baseDebug = baseDebug + "base2: [" + b2player[0] + "," + b2player[1] + "], ";
				else
					baseDebug = baseDebug + "base2: false, ";
				if (base3)
					baseDebug = baseDebug + "base3: [" + b3player[0] + "," + b3player[1] + "]";
				else
					baseDebug = baseDebug + "base3: false";

				gameDebug.println(baseDebug);
				FullPrintLine("base1: " + base1 + ", base2: " + base2 + ", base3: " + base3);
				FullPrintLine("Score: " + runs1 + "-" + runs2 + "\n");
			}
			
			if (runs1 > runs2 && pitchers1[0][0] == 0 && pitchers1[1][0] == 0 && pitchers1[2][0] == 0 && pitchers1[3][0] == 0)
			{
				pitchers1[p1index][0] = 1;
				
				pitchers2[0][0] = 0;
				pitchers2[1][0] = 0;
				pitchers2[2][0] = 0;
				pitchers2[3][0] = 0;
				pitchers2[4][0] = 0;
			}
			else if (runs1 == runs2)
			{
				pitchers1[0][0] = 0;
				pitchers1[1][0] = 0;
				pitchers1[2][0] = 0;
				pitchers1[3][0] = 0;
				pitchers1[4][0] = 0;
				
				pitchers2[0][0] = 0;
				pitchers2[1][0] = 0;
				pitchers2[2][0] = 0;
				pitchers2[3][0] = 0;
				pitchers2[4][0] = 0;
			}
			else if (runs2 > runs1 && pitchers2[0][0] == 0 && pitchers2[1][0] == 0 && pitchers2[2][0] == 0 && pitchers2[3][0] == 0)
			{
				pitchers2[p2index][0] = 1;
				
				pitchers1[0][0] = 0;
				pitchers1[1][0] = 0;
				pitchers1[2][0] = 0;
				pitchers1[3][0] = 0;
				pitchers1[4][0] = 0;
			}
			
			if (runs1 < runs2 && pitchers1[0][1] == 0 && pitchers1[1][1] == 0 && pitchers1[2][1] == 0 && pitchers1[3][1] == 0)
			{
				pitchers1[p1index][1] = 1;
				
				pitchers2[0][1] = 0;
				pitchers2[1][1] = 0;
				pitchers2[2][1] = 0;
				pitchers2[3][1] = 0;
				pitchers2[4][1] = 0;
			}
			else if (runs1 == runs2)
			{
				pitchers1[0][1] = 0;
				pitchers1[1][1] = 0;
				pitchers1[2][1] = 0;
				pitchers1[3][1] = 0;
				pitchers1[4][1] = 0;
				
				pitchers2[0][1] = 0;
				pitchers2[1][1] = 0;
				pitchers2[2][1] = 0;
				pitchers2[3][1] = 0;
				pitchers2[4][1] = 0;
			}
			else if (runs2 < runs1 && pitchers2[0][1] == 0 && pitchers2[1][1] == 0 && pitchers2[2][1] == 0 && pitchers2[3][1] == 0)
			{
				pitchers2[p2index][1] = 1;
				
				pitchers1[0][1] = 0;
				pitchers1[1][1] = 0;
				pitchers1[2][1] = 0;
				pitchers1[3][1] = 0;
				pitchers1[4][1] = 0;
			}
			
			pitchers1[p1index][2] = 1;
			pitchers2[p2index][2] = 1;
			
			if (outs == 3)
			{
				outs = 0;
				base1 = false;
				base2 = false;
				base3 = false;
				inning++;
				
				FullPrintLine("base1: " + base1 + ", base2: " + base2 + ", base3: " + base3);
				FullPrintLine("Score: " + runs1 + "-" + runs2 + "\n");	
				FullPrintLine("-----------------------\n");

				//check if game is over
				if (runs1 > runs2 && inning > 19 && inning%2 == 1)
				{
					extras = false;
				}
				
				if (inning < 18 || (runs2 == runs1) || (inning % 2 == 0 && runs1 > runs2))
				{
					if(inning % 2 == 0)
					{
						FullPrintLine("BOTTOM INNING " + inning/2 + "\n");
					}
					else
					{
						FullPrintLine("TOP INNING " + (inning+1)/2 + "\n");
					}
				}

				//change pitcher
				if (inning == 5 || inning == 9 || inning == 13 || inning == 15 || inning == 17)
				{
					p2index++;
					FullPrintLine("Team 2 pitcher change to " + team2pitchers[p2index] + "\n");
				}
				if (inning == 6 || inning == 10 || inning == 14 || inning == 16 || inning == 18)
				{
					p1index++;
					FullPrintLine("Team 1 pitcher change to " + team1pitchers[p1index] + "\n");
				}
			}
			
			if (pitchers1[4][4] == 1 && runs1 <= runs2)
			{
				pitchers1[4][4] = 0;
			}
			
			if (pitchers2[4][4] == 1 && runs2 <= runs1)
			{
				pitchers2[4][4] = 0;
			}
			
			if (inning == 19)
			{
				if (!extras && runs1 != runs2 || (runs2 > runs1 && inning%2 == 0))
				{
					if (pitchers1[4][0] == 1)
					{
						pitchers1[4][4] = 0;
						pitchers1[4][5] = 0;
					}
					
					if (pitchers2[4][0] == 1)
					{
						pitchers2[4][4] = 0;
						pitchers2[4][5] = 0;
					}
					
					FullPrintLine("Innings: " + ((inning)/2));
					FullPrintLine("Team 1 hits: " + hits1);
					FullPrintLine("Team 2 hits: " + hits2);
					FullPrintLine("Team 1 runs: " + runs1);
					FullPrintLine("Team 2 runs: " + runs2 + "\n");
					game = false;
				}
				else
				{
					extras = true;
				}
				
				if (!game)
				{
					FullPrintLine("Player\tAB\tR\tH\t2B\t3B\tHR\tRBI\tBB\tSO\t\t\t\t\t" + LocalDate.now());
					
					for (int i = 0; i < team1.length; i++)
					{
						FullPrint(team1batters[i] + "\t");
						for(int j = 0; j < team1[0].length; j++)
						{
							FullPrint(team1[i][j] + "\t");
						}
						if (i == 0)
							FullPrint("\t\t\t\t" + n1);
						FullPrintLine("");
					}
					
					FullPrintLine("");
					FullPrintLine("Player\tW\tL\tG\tGS\tSV\tSVO\tOUT\tH\tR\tHR\tBB\tSO\tBF");
	
					for (int i = 0; i < pitchers1.length; i++)
					{
						FullPrint(team1pitchers[i] + "\t");
						for(int j = 0; j < pitchers1[0].length; j++)
						{
							FullPrint(pitchers1[i][j] + "\t");
						}
						FullPrintLine("");
					}
					
					FullPrintLine("");
					FullPrintLine("Player\tAB\tR\tH\t2B\t3B\tHR\tRBI\tBB\tSO\t\t\t\t\t" + LocalDate.now());
					
					for (int i = 0; i < team2.length; i++)
					{
						FullPrint(team2batters[i] + "\t");
						for(int j = 0; j < team2[0].length; j++)
						{
							FullPrint(team2[i][j] + "\t");
						}
						if (i == 0)
							FullPrint("\t\t\t\t" + n2);
						FullPrintLine("");
					}
					
					FullPrintLine("");
					FullPrintLine("Player\tW\tL\tG\tGS\tSV\tSVO\tOUT\tH\tR\tHR\tBB\tSO\tBF");
	
					for (int i = 0; i < pitchers2.length; i++)
					{
						FullPrint(team2pitchers[i] + "\t");
						for(int j = 0; j < pitchers2[0].length; j++)
						{
							FullPrint(pitchers2[i][j] + "\t");
						}
						FullPrintLine("");
					}
					
					if (pitchers1[p1index][6] % 3 == 0 && pitchers1[p1index][12]-1 + pitchers1[p1index][8]*2.5 > 35)
						gameOutput.println("CHECK DEBUG: Team 1 Pitcher checkval > 35");
					if (pitchers2[p2index][6] % 3 == 0 && pitchers2[p2index][12]-1 + pitchers2[p2index][8]*2.5 > 35)
						gameOutput.println("CHECK DEBUG: Team 2 Pitcher checkval > 35");
					for (int i = 0; i <= 3; i++)
					{
						if (pitchers1[i][2] == 1 && pitchers1[i][12] == 0)
							gameOutput.println("CHECK DEBUG: Team 1 Pitcher " + (i+1) + " incorrectly registered for appearance");
						if (pitchers2[i][2] == 1 && pitchers2[i][12] == 0)
							gameOutput.println("CHECK DEBUG: Team 2 Pitcher " + (i+1) + " incorrectly registered for appearance");
					}
				}
			}
		}
		
		gameOutput.close();
		gameDebug.close();
		file1.close();
		file2.close();
	}
	
	public static void FullPrintLine(String s)
	{
		System.out.println(s);
		gameOutput.println(s);
		gameDebug.println(s);
	}
	
	public static void FullPrint(String s)
	{
		System.out.print(s);
		gameOutput.print(s);
		gameDebug.print(s);
	}
}
