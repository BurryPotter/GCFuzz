import java.lang.reflect.Field;
import java.lang.management.ManagementFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.ServerSocket;

//
// Try to replicate a pthread_suspend during a fork/exec
//
class FireHunter implements Runnable 
{
   private static int portNumber = 10010;
   private static int signalNumber = 17;  // SIGUSR2
   private static String command = "/bin/whoami";
   private static int numThreads = 25;
   private static boolean socketThreadNeedsRestart = false;
   private int type;
   private static int pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
   public static int numSignalsPosted = 0;
   public static int numFilesDeleted = 0;
   public static int signalsPosted() { return numSignalsPosted; }
   public static int filesDeleted() { return numFilesDeleted; }
   public FireHunter(int type) {
      this.type = type;
   }
   public static int getPid(Process process) {
      if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
         try {
            Field fPid = process.getClass().getDeclaredField("pid");
            if (!fPid.isAccessible()) {
               fPid.setAccessible(true);
            }
            return fPid.getInt(process);
         } catch (Exception e) {
            System.out.println("001");
            System.out.println("Exception " + e);
            return -1;
         }
      }
      return -2;
   }
   public static int deleteFiles() {
      int numFilesDeleted = 0;
      String currentDir = System.getProperty("user.dir");
      File folder = new File(currentDir);
      File[] filesToBeDeleted = folder.listFiles( new FilenameFilter() {
         @Override
         public boolean accept( File pathname, String name ) {
            return (name.startsWith("java" + pid) && name.endsWith(".eprof"));
         }
      } );
      for ( File file : filesToBeDeleted ) {
         try {
            if ( !file.delete() ) {
               System.err.println( "Can't remove " + file.getAbsolutePath() );
            } else {
               numFilesDeleted++;
            }
         } catch (Exception e) {
            System.out.println("006");
            System.out.println("Exception " + e);
         }
      }
      return numFilesDeleted;
   }
   public void run() 
   {
      switch ( type ) {
         case 1:
            try {
               Process p = Runtime.getRuntime().exec(command);
               System.out.println("Created Process: " + getPid(p));
               p.waitFor();
        
            }
            catch(Throwable p) {
               System.out.println("002");
               System.out.println(p);
            }
            break;

         case 2:
            // post signal thread
            int oldSignalNumber = signalNumber;
            String cmd = new String("kill -" + signalNumber + " " + pid);
            while ( true ) {
               try {
                  if ( oldSignalNumber != signalNumber ) {
                     oldSignalNumber = signalNumber;
                     cmd = new String("kill -" + signalNumber + " " + pid);
                  }
                  System.out.println("Posting signal...(" + cmd + ")");
                  Process p = Runtime.getRuntime().exec(cmd);
                  numSignalsPosted++;
                  Thread.sleep(1000);
               }
               catch(Throwable p) {
                  System.out.println("003");
                  System.out.println(p);
               }
            }
        case 3:
            while ( true ) {
               try {
                  System.out.println("Deleting profile files ");
                  numFilesDeleted += deleteFiles();
                  Thread.sleep(30000);
               }
               catch(Throwable p) {
                  System.out.println("004");
                  System.out.println(p);
               }
            }
        case 4:
           try {
              boolean portNumberChanged = false;
              ServerSocket listener = new ServerSocket(portNumber);
              while (true) {
                 System.out.println("Waiting for connection...");
                 Socket socket = listener.accept();
                 System.out.println("got connection...");
                 try {
     	            BufferedReader cmdIn = new BufferedReader(
        	               new InputStreamReader(socket.getInputStream()));

                    String cmdInput;
                    portNumberChanged = false;
                    while ((!portNumberChanged) && 
                           ((cmdInput = cmdIn.readLine()) != null)) {
                       String[] args = cmdInput.split(" ");
                       int numPendingArgs = args.length;
                       int argIndex = 0;
                       System.out.println("Received command " + cmdInput);
                       while ( numPendingArgs > 0 ) {
                          // argument specified. Check if it is option ( prefixed with "-" )
                          if ( args[argIndex].charAt(0) != '-' ) {
                             // Not the option. Break the while loop
			                 break;
		                  }
	                      // Get the option name
	                      String optionName = args[argIndex].substring(1);
	                      if ( optionName.equals("newPortNumber") ) {
	                         numPendingArgs--;
                	         argIndex++;
	                         // close the previous connection and open new one
	                         System.out.print("Switching listner port from " + portNumber);
							 listener.close();
	                         portNumber = Integer.parseInt(args[argIndex]);
                    	     System.out.println(" to " + portNumber);
	                         // Create a new socket
	                         listener = new ServerSocket(portNumber);
	                         portNumberChanged = true;
    	                   } else if ( optionName.equals("numThreads") ) {
	                         System.out.print("Switching number of threads from " + numThreads);
	                         numPendingArgs--;
	                         argIndex++;
	                         numThreads = Integer.parseInt(args[argIndex]);
                    	     System.out.println(" to " + numThreads);
	                       } else if ( optionName.equals("signalNumber") ) {
	                         System.out.print("Switching signal number from " + signalNumber);
	                         numPendingArgs--;
	                         argIndex++;
                    	     signalNumber = Integer.parseInt(args[argIndex]);
                    	     System.out.println(" to " + signalNumber);
                	      } else if ( optionName.equals("shutdown") ) {
                             System.out.println("Shutting Down FireHunter Test");
                             System.exit(0);
	                         break;
	                      } else if ( optionName.equals("command") ) {
	                         numPendingArgs--;
	                         argIndex++;
                                 String ncmd = args[argIndex];
                                 // Copy the arguments
                                 argIndex++;
                                 while ( argIndex < args.length ) {
                                    ncmd += ( " " + args[argIndex] );
                                    argIndex++;
                                 }
                                 System.out.println("Switching cmd from \"" +
                                    command + "\" to \"" + ncmd + "\"");
                                 command = ncmd;
                                 numPendingArgs = 0;
	                      } else {
                	         System.out.println("Unrecognized option" + args[argIndex]);
	                         System.exit(2);
                	      } 
                          numPendingArgs--;
                          argIndex++;
                      }
                    } // while
                 } finally {
                    socket.close();
                 } // try
              } // while
           } catch(Throwable p) {
              System.out.println("007");
              System.out.println(p);
           } finally {
              try {
//                 listener.close();
              } catch(Throwable p) {
                 System.out.println("008");
                 System.out.println(p);
              }
           }
       }
   }

   public static void sendCmd(int portNumber, String cmdStr)
   {
      try {
         System.out.println("Trying to connect...");
         Socket sock = new Socket("localhost", portNumber);
         System.out.println("Conncted to server");
         PrintWriter cmdOut = new PrintWriter(sock.getOutputStream(),true);
         System.out.println(
            "Sending \"" + cmdStr + "\" to FireHunter process on port "
                      + portNumber);        
         cmdOut.println(cmdStr);
      } catch(Throwable p) {
         System.out.println("007");
         System.out.println(p);
      }
   }

   static class ShutDownHookThread extends Thread{   
      public void run(){  
         numFilesDeleted += deleteFiles() ;
         System.out.println("Number of Signals Posted = " + numSignalsPosted); 
         System.out.println("Number of Files Deleted  = " + numFilesDeleted); 
      }   
   }   

   public static void printUsage() {
      System.out.println(
" Usage: java FireHunter <options>\n\n" +
"    Where <options> is one of the following option:\n" +
"       -numThreads -> Number of threads to create\n" +
"       -portNumber -> Port number for sending/receiving commands\n" +
"       -signalNumber -> Signal number to post to this process\n" +
"       -command      -> Command line string for creating child process\n" +
"       -newPortNumber -> Change the listening port number\n" +
"       -modify        -> Send the new options to the target FireHunter\n" +
"       -shutdown      -> Shutdown the running instance of FireHunter\n" +
"\n" +
" Note: \n" +
"   - If -shutdown is specified, other than -portNumber, all other options are ignored\n" +
"   - If -modify is specified, the commands will be sent to the target \n" +
"         process to modify the runtime arguments\n"
      );
   }
   public static void main(String[] args) throws java.lang.InterruptedException 
   {
      int l_numThreads = -1;
      int l_portNumber = -1;
      int l_newPortNumber = -1;
      int l_signalNumber = -1;
      String l_cmd = "";
      boolean modify    = false;
      boolean shutdown  = false;
      int numPendingArgs = args.length;
      int argIndex = 0;
      
      if ( ( args.length == 1 ) && args[0].equals("-help") ) {
         printUsage();
         System.exit(1);
      }
      while ( numPendingArgs > 0 ) {
         // argument specified. Check if it is option ( prefixed with "-" )
         if ( args[argIndex].charAt(0) != '-' ) {
            // Not the option. Break the while loop
            break;
	 }
	 // Get the option name
	 String optionName = args[argIndex].substring(1);
	 if ( optionName.equals("portNumber") ) {
	    numPendingArgs--;
	    argIndex++;
	    l_portNumber = Integer.parseInt(args[argIndex]);
	 } else if ( optionName.equals("numThreads") ) {
	    numPendingArgs--;
	    argIndex++;
	    l_numThreads = Integer.parseInt(args[argIndex]);
	 } else if ( optionName.equals("signalNumber") ) {
	    numPendingArgs--;
	    argIndex++;
	    l_signalNumber = Integer.parseInt(args[argIndex]);
	 } else if ( optionName.equals("newPortNumber") ) {
	    numPendingArgs--;
	    argIndex++;
	    l_newPortNumber = Integer.parseInt(args[argIndex]);
	 } else if ( optionName.equals("shutdown") ) {
	    shutdown = true;
	    break;
	 } else if ( optionName.equals("modify") ) {
	    modify = true;
	 } else if ( optionName.equals("command") ) {
	    numPendingArgs--;
	    argIndex++;
            l_cmd = args[argIndex];
            break;
	 } else {
	    System.out.println("Unrecognized option" + args[argIndex]);
            printUsage();
	    System.exit(2);
	 }
         numPendingArgs--;
         argIndex++;
      }
      if ( shutdown ) {
         if ( l_portNumber != -1 ) {
            sendCmd(l_portNumber, "-shutdown");
         } else {
            sendCmd(portNumber, "-shutdown");
         }
         System.exit(0);
      }
      if ( modify ) {
         // Send new parameters to FireHunter
         String cmdString = " ";
         if ( l_numThreads != -1 ) {
            cmdString = "-numThreads " + l_numThreads + " ";
         }
	 if ( l_signalNumber != -1 ) {
	    cmdString += (" -signalNumber " + l_signalNumber + " ");
	 }
	 if ( l_newPortNumber != -1 ) {
	    cmdString += (" -newPortNumber " + l_newPortNumber + " " );
	 }
	 if ( l_cmd != "" ) {
	    cmdString += ( " -command " + l_cmd + " ");
            // pass any arguments to the command
            argIndex++;
            while ( argIndex < args.length ) {
               cmdString += ( args[argIndex] + " " );
               argIndex++;
            }
	 }
	 cmdString = cmdString.trim();
         if ( l_portNumber != -1 ) {
            sendCmd(l_portNumber, cmdString);
         } else {
            sendCmd(portNumber, cmdString);
         }
         System.exit(0);
      }
      // Copy the parameters
      if ( l_numThreads != -1 ) {
         numThreads = l_numThreads;
      }
      if ( l_signalNumber != -1 ) {
         signalNumber = l_signalNumber;
      }
      if ( l_newPortNumber != -1 ) {
         portNumber = l_newPortNumber;
      }
      if ( l_cmd != "" ) {
         String cmdString = " ";
         cmdString += ( l_cmd + " ");
         // pass any arguments to the command
         argIndex++;
         while ( argIndex < args.length ) {
            cmdString += ( args[argIndex] + " " );
            argIndex++;
         }
         command = cmdString;
      }
      // Install Shutdown Hook
      Runtime.getRuntime().addShutdownHook(new ShutDownHookThread());

      FireHunter obj = new FireHunter(1);
      FireHunter signalObj = new FireHunter(2);
      FireHunter rmObj = new FireHunter(3);
      Thread signalThread = new Thread(signalObj);
      signalThread.start();
      Thread rmThread = new Thread(rmObj);
      rmThread.start();
      FireHunter sockObj = new FireHunter(4);
      Thread  sockThrd = new Thread(sockObj);
      sockThrd.start();

      //Keep going forever as a stress test
      int iteration = 0;
      while ( true ) {
         int crntNumThreads = numThreads;
         Thread t[] = new Thread[crntNumThreads];
         // If need to restart socket thread, start it
         if ( socketThreadNeedsRestart ) {
            sockThrd = new Thread(sockObj);
            sockThrd.start();
         }
         System.out.println("Starting threads...");
         for(int i = 0; i < crntNumThreads; i++) {
            System.gc();
            t[i] = new Thread(obj);
            System.gc();
            t[i].start();
            System.gc();
            System.out.println("I S:" + iteration + " T:" + i);
            System.gc();
         }
         System.out.println("Waiting for threads to finish (may take 10 secs)");
         for(int i = 0; i < crntNumThreads; i++) {
            System.gc();
            t[i].join();
            System.gc();
            System.out.println("I T:" + iteration + " T:" + i);
            System.gc();
         }
         iteration++;
      }
   }
}
