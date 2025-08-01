import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GameGenerator
{
	public static PrintWriter gameOutput = null, gameDebug = null, statCompendium = null, finalScores = null;
	public static boolean printingStats = false, updatingTeams = true, writing = true;
	public static int tracker = 0;
	public static String date, tmpComp = "", compendiumText = "", printerLine = "";
	
	public static void main(String[] args) throws FileNotFoundException
	{
		LocalDate today = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-LLL-yyyy");

		if (args.length == 0)
		{
			System.out.println("Welcome to Egg's baseball sim! Try 'java GameGenerator.java game [team1] [team2]' to simulate a game.");
		}
		else if (args[0].equals("winprob"))
		{
			if (args.length == 1)
			{
				System.out.println("Try 'java GameGenerator.java winprob [team1] [team2]'.");
			}
			else
			{
				updatingTeams = false;
				writing = false;
				multipleGameSim(args[1], args[2], 7500);				
			}
		}
		else if (args[0].equals("game"))
		{
			if (args.length == 1)
			{
				System.out.println("Try 'java GameGenerator.java game [team1] [team2]'.");
			}
			else
			{
				date = today.format(formatter);
				playGame(args[1], args[2], false);
			}
		}
		else if (args[0].equals("scheduleSim"))
		{
			if (args.length == 1)
			{
				System.out.println("Try 'java GameGenerator.java scheduleSim [fileName]'.");
			}
			else
			{
				try {
					statCompendium = new PrintWriter("statCompendium.txt");
					finalScores = new PrintWriter("finalScores.txt");
				} catch (IOException e)
				{
					System.out.println("Couldn't create stat compendium.");
					e.printStackTrace();
					System.exit(0);
				}

				Scanner inputFile = new Scanner(new File(args[1]));

				while (inputFile.hasNext())
				{
					String matchup = inputFile.nextLine();
					String[] inputs = matchup.split("\t");

					date = inputs.length > 2 ? inputs[2] : today.format(formatter);
					playGame(inputs[0], inputs[1], true);
					printerLine = "";
					tmpComp = "";
				}

				statCompendium.println(compendiumText);

				inputFile.close();
				statCompendium.close();
				finalScores.close();
			}
		}
	}

	public static void multipleGameSim(String t1arg, String t2arg, int cycles) throws FileNotFoundException
	{
		int t1wins = 0, t2wins = 0;
		int t1runs = 0, t2runs = 0;
		int gameTracker = 0, percentTracker = 0;
		int percentValue = cycles / 100;

		for (int i = 0; i < cycles; i++)
		{
			int[] scores = playGame(t1arg, t2arg, false);
			if (scores[0] > scores[1])
				t1wins++;
			else
				t2wins++;

			t1runs += scores[0];
			t2runs += scores[1];

			gameTracker++;
			if (gameTracker == percentValue)
			{
				percentTracker++;
				System.out.println("Simulating ... " + percentTracker + "% complete!");
				gameTracker = 0;
			}
		}

		DecimalFormat df = new DecimalFormat("#.##");
		System.out.println("");
		System.out.println(t1arg + " wins: " + t1wins + " (" + df.format((double)100*t1wins/cycles) + "%)");
		System.out.println(t2arg + " wins: " + t2wins + " (" + df.format((double)100*t2wins/cycles) + "%)");
		System.out.println(t1arg + " runs / game: " + df.format((double)t1runs/cycles));
		System.out.println(t2arg + " runs / game: " + df.format((double)t2runs/cycles));
	}

	public static int[] playGame(String t1arg, String t2arg, boolean compilingStats) throws FileNotFoundException
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
		
		int[][] pitchers1 = new int[5][13]; //W|L|G|GS|SV|SVO|OUT|H|R|HR|BB|SO|BF
		int[][] pitchers2 = new int[5][13];
		
		pitchers1[0][3] = 1;
		pitchers2[0][3] = 1;
		
		int t1index = 0;
		int t2index = 0;
		
		int p1index = 0;
		int p2index = 0;
		
		int tempA = 0, tempB = 0, tempC = 0, tempD = 0;
		boolean pitchswapA = false, pitchswapB = false, pitchswapC = false, pitchswapD = false;
		
		String t1 = "", t2 = "";

		t1 = "Teams/" + t1arg + ".txt";
		t2 = "Teams/" + t2arg + ".txt";

		try {
			if (compilingStats && writing)
			{
				gameOutput = new PrintWriter("Games/" + t1arg + "_at_" + t2arg + "_" + date + "_" + tracker + ".txt");
				gameDebug = new PrintWriter("Games/Debug Files/" + t1arg + "_at_" + t2arg + "_" + date + "_" + tracker + "_DEBUG.txt");

				tracker++;
			}
			else if (writing)
			{
				gameOutput = new PrintWriter("Games/" + t1arg + "_at_" + t2arg + " " + date + ".txt");
				gameDebug = new PrintWriter("Games/Debug Files/" + t1arg + "_at_" + t2arg + " " + date + "_DEBUG.txt");
			}
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
		
		String[] team1starters = new String[4];
		String[] team2starters = new String[4];
		
		int[] t1startcontrolranks = new int[4];
		int[] t2startcontrolranks = new int[4];
		
		int[] t1startstuffranks = new int[4];
		int[] t2startstuffranks = new int[4];
		
		String[] team1pitchers = new String[5];
		String[] team2pitchers = new String[5];
		
		int[] t1controlranks = new int[5];
		int[] t2controlranks = new int[5];
		
		int[] t1stuffranks = new int[5];
		int[] t2stuffranks = new int[5];
		
		int p1 = -1;
		for (int i = 0; i < 18; i++)
		{
			if (i < 9)
			{
				team1batters[i] = file1.next() + " " + file1.next();
				t1contactranks[i] = file1.nextInt();
				t1powerranks[i] = file1.nextInt();
			}
			else if (i < 13)
			{
				team1starters[i-9] = file1.next() + " " + file1.next();
				t1startcontrolranks[i-9] = file1.nextInt();
				t1startstuffranks[i-9] = file1.nextInt();
			}
			else if (i < 17)
			{
				team1pitchers[i-12] = file1.next() + " " + file1.next();
				t1controlranks[i-12] = file1.nextInt();
				t1stuffranks[i-12] = file1.nextInt();
			}
			else
			{
				p1 = file1.nextInt();
			}
		}
		
		team1pitchers[0] = team1starters[p1];
		t1controlranks[0] = t1startcontrolranks[p1];
		t1stuffranks[0] = t1startstuffranks[p1];
		
		int p2 = -1;
		for (int i = 0; i < 18; i++)
		{
			if (i < 9)
			{
				team2batters[i] = file2.next() + " " + file2.next();
				t2contactranks[i] = file2.nextInt();
				t2powerranks[i] = file2.nextInt();
			}
			else if (i < 13)
			{
				team2starters[i-9] = file2.next() + " " + file2.next();
				t2startcontrolranks[i-9] = file2.nextInt();
				t2startstuffranks[i-9] = file2.nextInt();
			}
			else if (i < 17)
			{
				team2pitchers[i-12] = file2.next() + " " + file2.next();
				t2controlranks[i-12] = file2.nextInt();
				t2stuffranks[i-12] = file2.nextInt();
			}
			else
			{
				p2 = file2.nextInt();
			}
		}
		
		team2pitchers[0] = team2starters[p2];
		t2controlranks[0] = t2startcontrolranks[p2];
		t2stuffranks[0] = t2startstuffranks[p2];

		printerLine += "TOP INNING 1\n" + "\n";
		while (game)
		{
			if (inning % 2 == 1)
			{
				printerLine += team1batters[t1index] + ": ";
			}
			else
			{
				printerLine += team2batters[t2index] + ": ";
			}
			
			rng = (int)(Math.random()*100)+1;
			
			if (inning % 2 == 1)
			{
				if (false && writing)
				{
					System.out.print("rng1 = " + rng + " + " + t1contactranks[t1index] + " - " + t2controlranks[p2index] + " - 1");
					gameDebug.print("rng1 = " + rng + " + " + t1contactranks[t1index] + " - " + t2controlranks[p2index] + " - 1");
				}
				rng += t1contactranks[t1index];
				rng -= t2controlranks[p2index];
				rng -= 1;
			}
			else
			{
				if (false && writing)
				{
					System.out.print("rng1 = " + rng + " + " + t2contactranks[t2index] + " - " + t1controlranks[p1index] + " + 1");
					gameDebug.print("rng1 = " + rng + " + " + t2contactranks[t2index] + " - " + t1controlranks[p1index] + " + 1");
				}
				rng += t2contactranks[t2index];
				rng -= t1controlranks[p1index];
				rng += 1;
			}
			if (false && writing)
			{
				System.out.println(" = " + rng);
				gameDebug.println(" = " + rng);
			}
			
			if (rng <= 10 || (rng >= 16 && rng <= 28))
			{
				printerLine += "Strikeout" + "\n";
				
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
				printerLine += "Walk" + "\n";
				
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
				printerLine += "Hit by pitch" + "\n";
				
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
			else if ((rng >= 11 && rng <= 15) || (rng >= 29 && rng <= 68))
			{
				//contact (out)
				rng = (int)(Math.random()*100)+1;
				
				if (inning % 2 == 1)
				{
					if (false && writing)
					{
						System.out.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
						gameDebug.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
					}
					rng += t1powerranks[t1index];
					rng -= t2stuffranks[p2index];
					rng -= 1;
				}
				else
				{
					if (false && writing)
					{
						System.out.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
						gameDebug.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
					}
					rng += t2powerranks[t2index];
					rng -= t1stuffranks[p1index];
					rng += 1;
				}
				if (false && writing)
				{
					System.out.println(" = " + rng);
					gameDebug.println(" = " + rng);
				}

				if (rng <= 47)
				{
					printerLine += "Groundout" + "\n";
					
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
					printerLine += "Lineout" + "\n";
					outs++;
					
					if (inning % 2 == 1)
						pitchers2[p2index][6]++;
					else
						pitchers1[p1index][6]++;
				}
				else if (rng >= 59)
				{
					printerLine += "Flyout" + "\n";
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
					if (false && writing)
					{
						System.out.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
						gameDebug.print("rng2 = " + rng + " + " + t1powerranks[t1index] + " - " + t2stuffranks[p2index] + " - 1");
					}
					rng += t1powerranks[t1index];
					rng -= t2stuffranks[p2index];
					rng -= 1;
				}
				else
				{
					if (false && writing)
					{
						System.out.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
						gameDebug.print("rng2 = " + rng + " + " + t2powerranks[t2index] + " - " + t1stuffranks[p1index] + " + 1");
					}
					rng += t2powerranks[t2index];
					rng -= t1stuffranks[p1index];
					rng += 1;
				}
				if (false && writing)
				{
					System.out.println(" = " + rng);
					gameDebug.println(" = " + rng);
				}
				
				if (rng <= 65)
				{
					printerLine += "Single" + "\n";
					
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
				
				else if ((rng >= 66 && rng <= 78) || (rng >= 86 && rng <= 92))
				{
					printerLine += "Double" + "\n";
					
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
				else if ((rng >= 80 && rng <= 85) || rng >= 94)
				{
					printerLine += "Home run" + "\n";
					
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
				else if (rng == 79 || rng == 93)
				{
					printerLine += "Triple" + "\n";
					
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
			
			if (inning % 2 == 0 && false && writing)
				gameDebug.println("Team 1 pitcher (" + team1pitchers[p1index] + "): " + pitchers1[p1index][12] + " batters faced, " + pitchers1[p1index][8] + " runs allowed = " + pitchers1[p1index][12] + " + " + 2.5*(pitchers1[p1index][8]) + " = " + (pitchers1[p1index][12] + 2.5*(pitchers1[p1index][8]) + ", outs: " + outs));
			else if (false && writing)
				gameDebug.println("Team 2 pitcher (" + team2pitchers[p2index] + "): " + pitchers2[p2index][12] + " batters faced, " + pitchers2[p2index][8] + " runs allowed = " + pitchers2[p2index][12] + " + " + 2.5*(pitchers2[p2index][8]) + " = " + (pitchers2[p2index][12] + 2.5*(pitchers2[p2index][8]) + ", outs: " + outs));
			if (false && writing)
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

				if (false && writing)
					gameDebug.println(baseDebug);
				printerLine += "base1: " + base1 + ", base2: " + base2 + ", base3: " + base3 + "\n";
				printerLine += "Score: " + runs1 + "-" + runs2 + "\n" + "\n";
			}
			
			if (runs1 > runs2 && pitchers1[0][0] == 0 && pitchers1[1][0] == 0 && pitchers1[2][0] == 0 && pitchers1[3][0] == 0 && pitchers1[4][0] == 0)
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
			else if (runs2 > runs1 && pitchers2[0][0] == 0 && pitchers2[1][0] == 0 && pitchers2[2][0] == 0 && pitchers2[3][0] == 0 && pitchers2[4][0] == 0)
			{
				pitchers2[p2index][0] = 1;
				
				pitchers1[0][0] = 0;
				pitchers1[1][0] = 0;
				pitchers1[2][0] = 0;
				pitchers1[3][0] = 0;
				pitchers1[4][0] = 0;
			}
			
			if (runs1 < runs2 && pitchers1[0][1] == 0 && pitchers1[1][1] == 0 && pitchers1[2][1] == 0 && pitchers1[3][1] == 0 && pitchers1[4][1] == 0)
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
			else if (runs2 < runs1 && pitchers2[0][1] == 0 && pitchers2[1][1] == 0 && pitchers2[2][1] == 0 && pitchers2[3][1] == 0 && pitchers2[4][1] == 0)
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
			
			//change pitchers
			if (!pitchswapA && p2index == 0 && outs == 3 && (pitchers2[p2index][6] + 1.5*pitchers2[p2index][10] + 1.5*pitchers2[p2index][7] + 2*(pitchers2[p2index][8]) > 29))
			{
				tempA = inning;
				pitchswapA = true;
			}
			
			if (!pitchswapB && p1index == 0 && outs == 3 && (pitchers1[p1index][6] + 1.5*pitchers1[p1index][10] + 1.5*pitchers1[p1index][7] + 2*(pitchers1[p1index][8]) > 29))
			{
				tempB = inning;
				pitchswapB = true;
			}
			
			if (inning%2 == 1 && p2index == 0 && outs != 3 && (pitchers2[p2index][6] + 1.5*pitchers2[p2index][10] + 1.5*pitchers2[p2index][7] + 2*(pitchers2[p2index][8]) > 35))
			{
				pitchswapA = false;
				p2index++;
				printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
			}
			
			if (inning%2 == 0 && p1index == 0 && outs != 3 && (pitchers1[p1index][6] + 1.5*pitchers1[p1index][10] + 1.5*pitchers1[p1index][7] + 2*(pitchers1[p1index][8]) > 35))
			{
				pitchswapB = false;
				p1index++;
				printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
			}

			if ((p2index == 1 || p2index == 2 || p2index == 3 || p2index == 4) && (p2index == 4 || pitchers2[p2index+1][2] == 0) && !pitchswapC && outs == 3 && (pitchers2[p2index][6] + 1.25*pitchers2[p2index][10] + 1.5*pitchers2[p2index][7] + 2*(pitchers2[p2index][8]) > 5))
			{
				tempC = inning;
				pitchswapC = true;
			}

			if ((p1index == 1 || p1index == 2 || p1index == 3 || p1index == 4) && (p1index == 4 || pitchers1[p1index+1][2] == 0) && !pitchswapD && outs == 3 && (pitchers1[p1index][6] + 1.25*pitchers1[p1index][10] + 1.5*pitchers1[p1index][7] + 2*(pitchers1[p1index][8]) > 5))
			{
				tempD = inning;
				pitchswapD = true;
			}
			
			if ((p2index == 1 || p2index == 2 || p2index == 3 || p2index == 4) && (p2index == 4 || pitchers2[p2index+1][2] == 0) && !pitchswapC && outs != 3 && (pitchers2[p2index][6] + 1.25*pitchers2[p2index][10] + 1.5*pitchers2[p2index][7] + 2*(pitchers2[p2index][8]) > 12))
			{
				if (p2index < 4)
				{
					p2index++;
					printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
				}
				else if (pitchers2[1][2] == 0)
				{
					p2index = 1;
					printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
				}
				else if (pitchers2[2][2] == 0)
				{
					p2index = 2;
					printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
				}
				else if (pitchers2[3][2] == 0)
				{
					p2index = 3;
					printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
				}
			}

			if ((p1index == 1 || p1index == 2 || p1index == 3 || p1index == 4) && (p1index == 4 || pitchers1[p1index+1][2] == 0) && !pitchswapD && outs != 3 && (pitchers1[p1index][6] + 1.25*pitchers1[p1index][10] + 1.5*pitchers1[p1index][7] + 2*(pitchers1[p1index][8]) > 12))
			{
				if (p1index < 4)
				{
					p1index++;
					printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
				}
				else if (pitchers1[1][2] == 0)
				{
					p1index = 1;
					printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
				}
				else if (pitchers1[2][2] == 0)
				{
					p1index = 2;
					printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
				}
				else if (pitchers1[3][2] == 0)
				{
					p1index = 3;
					printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
				}
			}
			
			if (outs == 3)
			{
				outs = 0;
				base1 = false;
				base2 = false;
				base3 = false;
				inning++;
				
				printerLine += "base1: " + base1 + ", base2: " + base2 + ", base3: " + base3 + "\n";
				printerLine += "Score: " + runs1 + "-" + runs2 + "\n" + "\n";	
				printerLine += "-----------------------\n" + "\n";
				
				//check if game is over
				if (runs1 > runs2 && inning > 19 && inning%2 == 1)
				{
					extras = false;
				}
				
				if (inning < 18 || (runs2 == runs1) || (inning % 2 == 0 && runs1 > runs2))
				{
					if(inning % 2 == 0)
					{
						printerLine += "BOTTOM INNING " + inning/2 + "\n" + "\n";
					}
					else
					{
						printerLine += "TOP INNING " + (inning+1)/2 + "\n" + "\n";
					}
				}
				
				//put in closer
				if (inning == 17 && runs2 > runs1 && runs2 - runs1 <= 3 && p2index < 4 && (p2index > 0 || pitchers2[0][7] > 0))
				{
					p2index = 4;
					pitchers2[4][4] = 1;
					pitchers2[4][5] = 1;
					printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
					pitchswapA = false;
					pitchswapC = false;
				}
				
				//put in closer
				if (inning == 18 && runs1 > runs2 && runs1 - runs2 <= 3 && p1index < 4 && (p1index > 0 || pitchers1[0][7] > 0))
				{
					p1index = 4;
					pitchers1[4][4] = 1;
					pitchers1[4][5] = 1;
					printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
					pitchswapB = false;
					pitchswapD = false;
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
			
			if (inning >= 19 || (inning == 18 && runs2 > runs1))
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
					
					printerLine += "Innings: " + ((inning)/2) + "\n";
					printerLine += "Team 1 hits: " + hits1 + "\n";
					printerLine += "Team 2 hits: " + hits2 + "\n";
					printerLine += "Team 1 runs: " + runs1 + "\n";
					printerLine += "Team 2 runs: " + runs2 + "\n" + "\n";
					game = false;
				}
				else
				{
					extras = true;
				}
				
				if (!game)
				{
					if (compilingStats)
					{
						printingStats = true;
						finalScores.println(runs1 + "\t" + t1arg + "\t@\t" + t2arg + "\t" + runs2);
					}

					tmpComp += "Player\tAB\tR\tH\t2B\t3B\tHR\tRBI\tBB\tSO\t\t\t\t\t" + date + "\n";
					
					for (int i = 0; i < team1.length; i++)
					{
						tmpComp += team1batters[i] + "\t";
						for(int j = 0; j < team1[0].length; j++)
						{
							tmpComp += team1[i][j] + "\t";
						}
						if (i == 0)
							tmpComp += "\t\t\t\t" + t1arg;
						tmpComp += "" + "\n";
					}
					
					tmpComp += "" + "\n";
					tmpComp += "Player\tW\tL\tG\tGS\tSV\tSVO\tOUT\tH\tR\tHR\tBB\tSO\tBF" + "\n";
	
					for (int i = 0; i < pitchers1.length; i++)
					{
						tmpComp += team1pitchers[i] + "\t";
						for(int j = 0; j < pitchers1[0].length; j++)
						{
							tmpComp += pitchers1[i][j] + "\t";
						}
						tmpComp += "" + "\n";
					}
					
					tmpComp += "" + "\n";
					tmpComp += "Player\tAB\tR\tH\t2B\t3B\tHR\tRBI\tBB\tSO\t\t\t\t\t" + date + "\n";
					
					for (int i = 0; i < team2.length; i++)
					{
						tmpComp += team2batters[i] + "\t";
						for(int j = 0; j < team2[0].length; j++)
						{
							tmpComp += team2[i][j] + "\t";
						}
						if (i == 0)
							tmpComp += "\t\t\t\t" + t2arg;
						tmpComp += "" + "\n";
					}
					
					tmpComp += "" + "\n";
					tmpComp += "Player\tW\tL\tG\tGS\tSV\tSVO\tOUT\tH\tR\tHR\tBB\tSO\tBF" + "\n";
	
					for (int i = 0; i < pitchers2.length; i++)
					{
						tmpComp += team2pitchers[i] + "\t";
						for(int j = 0; j < pitchers2[0].length; j++)
						{
							tmpComp += pitchers2[i][j] + "\t";
						}
						tmpComp += "" + "\n";
					}

					if (compilingStats)
						printingStats = false;
					
					if (false && writing && pitchers1[p1index][6] % 3 == 0 && pitchers1[p1index][12]-1 + pitchers1[p1index][8]*2.5 > 35)
						gameOutput.println("CHECK DEBUG: Team 1 Pitcher checkval > 35");
					if (false && writing && pitchers2[p2index][6] % 3 == 0 && pitchers2[p2index][12]-1 + pitchers2[p2index][8]*2.5 > 35)
						gameOutput.println("CHECK DEBUG: Team 2 Pitcher checkval > 35");
					for (int i = 0; i <= 3; i++)
					{
						if (false && writing && pitchers1[i][2] == 1 && pitchers1[i][12] == 0)
							gameOutput.println("CHECK DEBUG: Team 1 Pitcher " + (i+1) + " incorrectly registered for appearance");
						if (false && writing && pitchers2[i][2] == 1 && pitchers2[i][12] == 0)
							gameOutput.println("CHECK DEBUG: Team 2 Pitcher " + (i+1) + " incorrectly registered for appearance");
					}
				}
			}
			
			if (inning == tempA + 2 && pitchswapA && game)
			{
				pitchswapA = false;
				p2index++;
				printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
			}
			
			if (inning == tempB + 2 && pitchswapB && game)
			{
				pitchswapB = false;
				p1index++;
				printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
			}
			
			if (inning == tempC + 2 && pitchswapC && game)
			{
				pitchswapC = false;
				
				if (p2index < 4)
				{
					if (inning > 16 && Math.abs(runs1-runs2) < 6 && pitchers2[1][2] == 0)
						p2index = 4;
					else
						p2index++;

					printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
				}
				else
				{
					if (pitchers2[1][2] == 0)
					{
						p2index = 1;
						printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
					}
					else if (pitchers2[2][2] == 0)
					{
						p2index = 2;
						printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
					}
					else if (pitchers2[3][2] == 0)
					{
						p2index = 3;
						printerLine += "Team 2 pitcher change to " + team2pitchers[p2index] + "\n" + "\n";
					}
				}
			}
			
			if (inning == tempD + 2 && pitchswapD && game)
			{
				pitchswapD = false;

				if (p1index < 4)
				{
					if (inning > 16 && Math.abs(runs1-runs2) < 6 && pitchers1[4][2] == 0)
						p1index = 4;
					else
						p1index++;

					printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
				}
				else
				{
					if (pitchers1[1][2] == 0)
					{
						p1index = 1;
						printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
					}
					else if (pitchers1[2][2] == 0)
					{
						p1index = 2;
						printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
					}
					else if (pitchers1[3][2] == 0)
					{
						p1index = 3;
						printerLine += "Team 1 pitcher change to " + team1pitchers[p1index] + "\n" + "\n";
					}
				}
			}
		}

		int[] scores = new int[2];

		System.out.println(inning);
		if (inning > 19 && (pitchers1[4][6] >= 8 || pitchers2[4][6] >= 8 || pitchers1[3][6] >= 9 || pitchers2[3][6] >= 9))
		{
			tmpComp = "";
			printerLine = "";
			scores = playGame(t1arg, t2arg, compilingStats);
		}
		else
		{
			compendiumText = tmpComp + "\n" + compendiumText;
			printerLine += tmpComp;

			System.out.print(printerLine);
			gameOutput.print(printerLine);
			gameDebug.print(printerLine);

			if (updatingTeams)
			{
				Scanner reader1 = new Scanner(new File(t1));
				ArrayList<String> array1 = new ArrayList<String>();
				while (reader1.hasNextLine())
				{
					array1.add(reader1.nextLine());
				}
				try {
					PrintWriter writer = new PrintWriter(t1);
					for (int i = 0; i < 13; i++)
					{
						writer.println(array1.get(i));					
					}

					int r1stats = t1controlranks[1] + t1stuffranks[1];
					int r2stats = t1controlranks[2] + t1stuffranks[2];
					int r3stats = t1controlranks[3] + t1stuffranks[3];
					int[] rsorted = new int[3];

					if (r3stats > r2stats)
					{
						if (r1stats > r3stats)
						{
							rsorted[0] = 1;
							rsorted[1] = 3;
							rsorted[2] = 2;
						}
						else if (r2stats > r1stats)
						{
							rsorted[0] = 3;
							rsorted[1] = 2;
							rsorted[2] = 1;
						}
						else
						{
							rsorted[0] = 3;
							rsorted[1] = 1;
							rsorted[2] = 2;
						}
					}
					else
					{
						if (r1stats > r2stats)
						{
							rsorted[0] = 1;
							rsorted[1] = 2;
							rsorted[2] = 3;
						}
						else if (r1stats > r3stats)
						{
							rsorted[0] = 2;
							rsorted[1] = 1;
							rsorted[2] = 3;
						}
						else
						{
							rsorted[0] = 2;
							rsorted[1] = 3;
							rsorted[2] = 1;				
						}
					}

					ArrayList<String> pitched = new ArrayList<String>();
					ArrayList<String> didntPitch = new ArrayList<String>();

					for (int i = 0; i < 3; i++)
					{
						if (pitchers1[rsorted[i]][2] == 1)
						{
							pitched.add(array1.get(rsorted[i]+12));
						}
						else
						{
							didntPitch.add(array1.get(rsorted[i]+12));
						}
					}

					didntPitch.forEach((n) -> writer.println(n));
					pitched.forEach((n) -> writer.println(n));

					writer.println(array1.get(16));

					if (p1 == 3)
					{
						writer.print("0");
					}
					else
					{
						writer.print(p1+1);
					}
					writer.close();
				}
				catch (IOException e)
				{
					System.out.println("Couldn't write to file.");
				}
				
				Scanner reader2 = new Scanner(new File(t2));
				ArrayList<String> array2 = new ArrayList<String>();
				while (reader2.hasNextLine())
				{
					array2.add(reader2.nextLine());
				}
				try {
					PrintWriter writer = new PrintWriter(t2);
					for (int i = 0; i < 13; i++)
					{
						writer.println(array2.get(i));					
					}

					int r1stats = t2controlranks[1] + t2stuffranks[1];
					int r2stats = t2controlranks[2] + t2stuffranks[2];
					int r3stats = t2controlranks[3] + t2stuffranks[3];
					int[] rsorted = new int[3];

					if (r3stats > r2stats)
					{
						if (r1stats > r3stats)
						{
							rsorted[0] = 1;
							rsorted[1] = 3;
							rsorted[2] = 2;
						}
						else if (r2stats > r1stats)
						{
							rsorted[0] = 3;
							rsorted[1] = 2;
							rsorted[2] = 1;
						}
						else
						{
							rsorted[0] = 3;
							rsorted[1] = 1;
							rsorted[2] = 2;
						}
					}
					else
					{
						if (r1stats > r2stats)
						{
							rsorted[0] = 1;
							rsorted[1] = 2;
							rsorted[2] = 3;
						}
						else if (r1stats > r3stats)
						{
							rsorted[0] = 2;
							rsorted[1] = 1;
							rsorted[2] = 3;
						}
						else
						{
							rsorted[0] = 2;
							rsorted[1] = 3;
							rsorted[2] = 1;				
						}
					}

					ArrayList<String> pitched = new ArrayList<String>();
					ArrayList<String> didntPitch = new ArrayList<String>();

					for (int i = 0; i < 3; i++)
					{
						if (pitchers2[rsorted[i]][2] == 1)
						{
							pitched.add(array2.get(rsorted[i]+12));
						}
						else
						{
							didntPitch.add(array2.get(rsorted[i]+12));
						}
					}

					didntPitch.forEach((n) -> writer.println(n));
					pitched.forEach((n) -> writer.println(n));

					writer.println(array2.get(16));

					if (p2 == 3)
					{
						writer.print("0");
					}
					else
					{
						writer.print(p2+1);
					}

					writer.close();
				}
				catch (IOException e)
				{
					System.out.println("Couldn't write to file.");
				}

				scores[0] = runs1;
				scores[1] = runs2;

				reader1.close();
				reader2.close();
				gameOutput.close();
				gameDebug.close();
			}
		}

		file1.close();
		file2.close();

		return scores;
	}
}