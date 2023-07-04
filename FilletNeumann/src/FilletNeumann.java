import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class FilletNeumann {


	public int[] Memory = new int [2048]; 
	//Addresses from 0 to 1023 contain the program instructions.
	// Addresses from 1024 to 2047 contain the data
	public int [] registerFile = new int [32]; 
	public static final int R0  =0;
	public int PC = 0;
	public int clkCycles;
	public int InstrCount;
	public PipelineReg IF_IDReg = new PipelineReg();
	public PipelineReg ID_EXReg = new PipelineReg();
	public PipelineReg EX_MEMReg = new PipelineReg();
	public PipelineReg MEM_WBReg = new PipelineReg();
	public boolean flagID = false;
	public boolean flagEX = false;
	public boolean flagMEM = false;
	public boolean flagWB = false;	
	public boolean finishcycles = true ; // to check if we reached the last instruction
	public boolean lastfetch = false ;
	public boolean lastdecode = false ;
	public boolean lasteexcute = false ;
	public boolean lastmemory = false ;
	public boolean jumpmem_flag = false ;
	public boolean jumpwb_flag = false ;
	
	
	public FilletNeumann() throws IOException {
		InstrCount = 0;
		registerFile[0]= 0 ;
		translateInstrFile("instructionsFile.txt");		
		clkCycles = 7 + ((InstrCount - 1) * 2) ;         //Number of clock cycles: 7 + ((n − 1) ∗ 2), where n = number of instructions
		start();
	}
	
	public void start() {
//		int opcode = 0;  // bits31:28
//        int r1 = 0;      // bits27:23
//        int r2 = 0;      // bit22:18
//        int r3 = 0;      // bits17:13
//        int shamt = 0;   // bits12:0
//        int imm = 0;     // bits17:0
//        int address = 0; // bits27:0
        
		int ClkCycles = 1 ;
		System.out.println("WELCOME TO OUR AMAZING FILLET NEUMANN  ;)   ");
		 while(finishcycles ) {
			
			
			System.out.println("------------------------------------------");
			System.out.println("clk cycle:" + ClkCycles );
			
			if ((ClkCycles %2  == 1) &&  (ClkCycles >= 7) ) {
				if(!jumpwb_flag)
					writeBack();
				else
					jumpwb_flag = false ;
			}
			if ((ClkCycles %2  == 0) &&  (ClkCycles >= 6) && ! lastmemory ) {
				if(! jumpmem_flag)
					memoryAccess();
				else
					jumpmem_flag = false ;
			}
			if ((ClkCycles >= 4 && !lasteexcute)   )   {
				if (ClkCycles %2  == 1)
					execute();
				else
					System.out.println("Executing Instruction : "+ID_EXReg.instruction_number);
			}
			if ( ClkCycles >= 2  && !lastdecode) {
				if(ClkCycles % 2 == 1)
					decode();
				else
					System.out.println("Decoding Instruction : "+IF_IDReg.instruction_number);
			}
			if ( ClkCycles % 2 == 1 && !lastfetch)			
				fetch();
			ClkCycles++;
	}
		for(int i= 0; i< registerFile.length ; i++) {
			System.out.println("R" +i+ ":" + registerFile[i]);
		}
		for(int i= 0 ; i< Memory.length; i++) {
			System.out.println("Memory[" + i + "]:" + Memory[i]);
		}
		
	}
	

	public void fetch() {
				System.out.println("#FETCH STAGE#");
				System.out.println("-fetching instruction "+ (PC+1));
				if((PC+1) == InstrCount)
					lastfetch = true ;
		        IF_IDReg.instruction_number = PC +1 ;
		        
		        IF_IDReg.instruction = Memory[PC];
				PC++;
				if (flagID)
					flagID = false;
				
					
	}
	
	public void decode ( ) {
		
		if(flagID)
			return;
		
		System.out.println("#DECODE STAGE#");
		System.out.println("-decoding instruction " + IF_IDReg.instruction_number);
		System.out.println("input values : instr= " + IF_IDReg.instruction );
		
		int instruction = IF_IDReg.instruction;
        String result=Integer.toBinaryString(instruction);
        String result32 = String.format("%32s", result).replaceAll(" ", "0");  // 32-bit Integer
        ID_EXReg.instruction_number = IF_IDReg.instruction_number ;
        ID_EXReg.opcode=Integer.parseInt(result32.substring(0,4),2);
        ID_EXReg.r1=Integer.parseInt(result32.substring(4,9),2);                   
        ID_EXReg.r2=Integer.parseInt(result32.substring(9,14),2);
        ID_EXReg.r3=Integer.parseInt(result32.substring(14,19),2);
        ID_EXReg.shamt=Integer.parseInt(result32.substring(19,32),2);
        ID_EXReg.imm = Integer.parseInt(result32.substring(14, 32), 2);
        ID_EXReg.address=Integer.parseInt(result32.substring(4,32),2);
        if(lastfetch)
        	lastdecode =true ;
        if (flagEX)
        	flagEX = false;
	}
	
	
	public void execute( ) {
	
		if (flagEX)
			return;
		System.out.println("#EXECUTE STAGE#");
		System.out.println("-executing instruction " + ID_EXReg.instruction_number);
		System.out.println("input values:  rd=" + ID_EXReg.r1+
										 " /rs=" +ID_EXReg.r2+
										  " /rt=" +ID_EXReg.r3+
										  " /opcode=" +ID_EXReg.opcode+
										  " /shamt=" +ID_EXReg.shamt+
										  " /imm= " +ID_EXReg.imm+
										  " /address=" +ID_EXReg.address );
		EX_MEMReg.instruction_number = ID_EXReg.instruction_number ;

		
		int result= 0;
		int r1 = ID_EXReg.r1;
		int r2 = ID_EXReg.r2;
		int r3 = ID_EXReg.r3;
		int opcode = ID_EXReg.opcode;
		int shamt = ID_EXReg.shamt;
		int imm = ID_EXReg.imm;
		int address = ID_EXReg.address;
		
		switch(opcode) {
		  case 0:	   //ADD
		  {
			  if ((r2 == 0)  && (r3 == 0) )
				  result = R0 + R0;
			  else if (r2 == 0 )
				  result = R0 + registerFile[r3];
			  else if (r3 == 0 )
				  result = registerFile[r2] + R0;
			  else 
				  result = registerFile[r2] + registerFile[r3];   
			EX_MEMReg.instrType = "ADD";
			EX_MEMReg.r1 = r1;
			                  
		  }
		    break;
		  case 1:      //SUB
		  {
			  if ( (r2==0)  &&  (r3==0))
				  result = R0 - R0;
			  else if ( r2 == 0 )
				  result = R0 - registerFile[r3];
			  else if (r3 == 0 )
				  result = registerFile[r2] - R0;
			  else 
				  result = registerFile[r2] - registerFile[r3];      
			  EX_MEMReg.instrType = "SUB";
			  EX_MEMReg.r1 = r1;                                        
		  }
		    break;
		  case 2:      //MUL
		  {
			  if ( (r2==0)  &&  (r3==0))
				  result = R0 * R0;
			  if ( r2 == 0 )
				  result = R0 * registerFile[r3];
			  else if (r3 == 0 )
				  result = registerFile[r2] * R0;
			  else 
				  result = registerFile[r2] * registerFile[r3];    
			  EX_MEMReg.instrType = "MUL";
			  EX_MEMReg.r1 = r1;                                      
		  }
		    break;
		  case 3:      //MOVI
		  {
			//  System.out.println("MOVE I , Imm value = "+imm);
			  result = imm;     
			  EX_MEMReg.instrType = "MOVI";
			  EX_MEMReg.r1 = r1;  											
		  }
		    break;
		  case 4:      //JEQ                                                                                //handle control hazard????????????/
		  {
			  EX_MEMReg.instrType = "JEQ";
			  if(registerFile[r1] == registerFile[r2]) {                  
				  PC = PC -1 + imm;                                           //ask TA ??????
				  flagID = true;
				  flagEX = true;
				  flagMEM = true;
				  flagWB = true;				  
			  }
			  //else
				  //result = ID_EXReg.execResult;
			  
		  }	  
		    break;
		  case 5:      //AND
		  {
			  if   ( (r2==0)  &&  (r3==0))
				  result = R0 & R0;
			  else if ( r2 == 0 )
				  result = R0 & registerFile[r3];
			  else if (r3 == 0 )
				  result = registerFile[r2] & R0;
			  else 
				  result = registerFile[r2] & registerFile[r3];   
			  EX_MEMReg.instrType = "AND";
			  EX_MEMReg.r1 = r1;											
		  }
		    break;
		  case 6:      //XORI
		  {
			  if (r2 ==0)
				  result = R0 ^ imm;
			  else
			  	result = registerFile[r2] ^ imm;  
			  EX_MEMReg.instrType = "XORI";
			  EX_MEMReg.r1 = r1;
			  												
		  }
		    break;
		  case 7:      //JMP                                                                                  //handle control hazard ???????
		  {
			  System.out.println("oldpc:" + (PC-2));
			  String tempPC=Integer.toBinaryString(PC-2);
		      String tempPC32 = String.format("%32s", tempPC).replaceAll(" ", "0");  // 32-bit Integer
		      String PC4bits = tempPC32.substring(0,4);

		      
		      String tempAddress=Integer.toBinaryString(address);
		      String tempAddress28 = String.format("%28s", tempAddress).replaceAll(" ", "0");  // 28-bit Integer
		      
		      String newPC = PC4bits+tempAddress28;
		      
		      
		      PC =Integer.parseInt(newPC,2);			
		    //  System.out.println("this is newpc for JMP: "+ PC);
		    //  clkCycles+=2;
		      flagID = true;
			  flagEX = true;
			  flagMEM = true;
			  flagWB = true;
		      EX_MEMReg.instrType = "JMP";
		  }
		    break;
		  case 8:      //LSL
		  {
			  if (r2 ==0)
				  result = R0 << shamt;
			  else 
			  	result = registerFile[r2]  <<  shamt ;  
			  
			  EX_MEMReg.instrType = "LSL";
			  EX_MEMReg.r1 = r1;		  											
		  }
		    break;
		  case 9:      //LSR
		  {
			  if ( r2 == 0)
				  result = R0 >>> shamt;
			  else
				  result = registerFile[r2] >>> shamt;      
				
	         EX_MEMReg.instrType = "LSR";
	         EX_MEMReg.r1 = r1;											
		  }
		    break;
		  case 10:      //MOVR
		  {
			  int memAddress1;
			  if (r2 ==0 )
				  memAddress1 = R0 + imm + 1024;
			  else 
				   memAddress1 = registerFile[r2] + imm + 1024;
			  
			  EX_MEMReg.instrType = "MOVR";
			  EX_MEMReg.r1 = r1;
			  EX_MEMReg.memAddress = memAddress1;
		  }
		    break;
		  case 11:      //MOVM
		  {
			  int memAddress2;
			  if (r2==0)
				  memAddress2 = R0 + imm + 1024;
			  else
			      memAddress2 = registerFile[r2] + imm +1024;
			  EX_MEMReg.instrType = "MOVM";
			  EX_MEMReg.r1 = r1;
			  EX_MEMReg.memAddress = memAddress2;	  
		  }
		    break;   
		  default:break ;
		    // code block
		}
		
		EX_MEMReg.execResult= result;
		if (flagMEM)
			flagMEM = false;
		if(lastdecode)
			lasteexcute = true ;
	}
	
	
	public void memoryAccess() {
		if(flagMEM)
			return;
		System.out.println("#MEMORY STAGE#");
		System.out.println("-memory accessing instruction " + EX_MEMReg.instruction_number);
		System.out.println("input values: instrType= " +EX_MEMReg.instrType +
										" /memAddress=" + EX_MEMReg.memAddress +
										 " /rd= " +  EX_MEMReg.r1 + 
										 " /execResult=" + EX_MEMReg.execResult);
		MEM_WBReg.instruction_number = EX_MEMReg.instruction_number ;
		

		String instrType = EX_MEMReg.instrType;
		int memAddress = EX_MEMReg.memAddress;
		int r1 = EX_MEMReg.r1;
		MEM_WBReg.instrType = EX_MEMReg.instrType;
		MEM_WBReg.r1 = EX_MEMReg.r1;
		MEM_WBReg.execResult = EX_MEMReg.execResult;
		
		if(instrType.equals("JMP") || instrType.equals("JEQ") )
			jumpmem_flag = true ;
		
		
		if ( instrType == "MOVR") {
			MEM_WBReg.execResult = Memory[memAddress];     		
			System.out.println("value of Memory["+memAddress+"] is loaded  (value="+Memory[memAddress] + ")");
		}
		
		else  if ( instrType == "MOVM")  {
			Memory[memAddress]= registerFile[r1];
			System.out.println("Memory["+memAddress+"] is updated to -> regFile["+r1 +"]"+" (value=" +registerFile[r1]+")"  );
		}		
		if (flagWB)
			flagWB = false;
		if(lasteexcute)
			lastmemory = true ;
	}
	

	public void writeBack() {
		
		if(flagWB)
			return;
		System.out.println("#WRITEBACK STAGE#");
		System.out.println("-writing back instruction " + MEM_WBReg.instruction_number);
		System.out.println("input values: instrType=" + MEM_WBReg.instrType+
										                        " /rd=" + MEM_WBReg.r1+
										                        " /execResult=" + MEM_WBReg.execResult );

		String instrType = MEM_WBReg.instrType;
		int r1 = MEM_WBReg.r1;
		int execResult = MEM_WBReg.execResult;
		if ( (instrType != "JEQ")    &&  (instrType != "JMP")  && (instrType != "MOVM")) {
			if (r1 == 0) 
				System.out.println("R0 cant be modified !!");
			else {
				System.out.println("Register R"+r1+" Changed value from "+registerFile[r1] +" to "+execResult);
				registerFile[r1] = execResult;
			}
		}
		else
			jumpwb_flag = true ;
		if(MEM_WBReg.instruction_number == InstrCount) //if this is the last instruction end the loop
			finishcycles = false ;
		
	}
	
	
	
	public String getRegBits(String regNum) {
		switch(regNum) {
	      case "R0": return"00000";  
		  case "R1": return "00001"; 
		  case "R2": return"00010";
		  case "R3": return "00011";
		  case "R4": return "00100";
		  case "R5": return "00101";
		  case "R6": return "00110";
		  case "R7": return "00111";
		  case "R8": return "01000";
		  case "R9": return "01001";
		  case "R10": return "01010";
		  case "R11": return "01011";
		  case "R12": return "01100";
		  case "R13": return "01101";
		  case "R14": return "01110";
		  case "R15": return "01111";
		  case "R16": return "10000";
		  case "R17": return "10001";
		  case "R18": return "10010";
		  case "R19": return "10011";
		  case "R20": return "10100";
		  case "R21": return "10101";
		  case "R22": return "10110";
		  case "R23": return "10111";
		  case "R24": return "11000";
		  case "R25": return "11001";
		  case "R26": return "11010";
		  case "R27": return "11011";
		  case "R28": return "11100";
		  case "R29": return "11101";
		  case "R30": return "11110";
		  case "R31": return "11111";
		  default : return"";
		}
	}
	
	
	public String getShamtBits(String shamtNum) {	
		int shamt = Integer.parseInt(shamtNum);
	    String tempShamt=Integer.toBinaryString(shamt);
 	    String tempShamt13 = String.format("%13s", tempShamt).replaceAll(" ", "0");  // 13-bit Integer
		return tempShamt13;
		
	}
	
	public String getImmBits(String immNum) {
		int immediate = Integer.parseInt(immNum);
	    String tempImmediate=Integer.toBinaryString(immediate);
 	    String tempImmediate18 = String.format("%18s", tempImmediate).replaceAll(" ", "0");  // 18-bit Integer
 	    return tempImmediate18;
	}

	public String getJumpAddBits (String addressBits) {
		int address = Integer.parseInt(addressBits);
	    String tempAddress=Integer.toBinaryString(address);
 	    String tempAddress28 = String.format("%28s", tempAddress).replaceAll(" ", "0");  
 	    return tempAddress28;
	}
	
	public void translateInstrFile(String filePath) throws IOException {
		String instruction= "";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while (line != null) {
			String[] content = line.split(" ");
			instruction = "" ;
			//translating content[0]  -> instruction type 
			switch (content[0]) {
			   case "ADD": instruction += "0000";  break;
			   case "SUB":  instruction+= "0001";   break;
			   case "MUL":  instruction+= "0010";  break;
			   case "MOVI":  instruction+= "0011";break;
			   case "JEQ":  instruction+= "0100";    break;
			   case "AND":  instruction+= "0101";  break;
			   case "XORI":  instruction+= "0110";  break;
			   case "JMP":  instruction+= "0111";   break;
			   case "LSL":  instruction+= "1000";    break;
			   case "LSR":  instruction+= "1001";   break;
			   case "MOVR":  instruction+= "1010";break;
			   case "MOVM":  instruction+= "1011";break;
			}
			
			//translate the rest of the instr according to the instr type
			
			if (  (content[0].equals("ADD"))   ||   (content[0].equals("SUB"))   ||  (content[0].equals("MUL"))   ||   (content[0].equals("AND"))    ) {
				instruction+= getRegBits(content[1]) + getRegBits(content[2]) + getRegBits(content[3]) + "0000000000000";
			}
			
			else if (  (content[0].equals("LSL"))   ||   (content[0].equals("LSR")) ) {
				instruction+= getRegBits(content[1]) + getRegBits(content[2]) + "00000" + getShamtBits(content[3]);
			}
			
			else if (content[0].equals("MOVI")) {
				instruction+= getRegBits(content[1]) + "00000" + getImmBits(content[2]);
			}
			
			else if (  (content[0].equals("JEQ"))   ||   (content[0].equals("XORI"))   ||  (content[0].equals("MOVR"))   ||   (content[0].equals("MOVM"))    ) {
				instruction+= getRegBits(content[1])+ getRegBits(content[2]) + getImmBits(content[3]);
			}
			
			else if (content[0].equals("JMP")) {
				instruction+= getJumpAddBits(content[1]);
			}
			
			int instrReady = converttwoscomplement(instruction);       
			
			Memory[InstrCount++]= instrReady;  // place instruction in memory then increment instruction count
			line = br.readLine(); 
	   }
	   br.close();
	}	
	
	public static int converttwoscomplement(String instruction) {        
		int result = 0 ;
		
		if(instruction.charAt(0) == '1'   )
		{
			//num is negative
			instruction = instruction.replaceAll("0", "t");
			instruction = instruction.replaceAll("1", "0");
			instruction = instruction.replaceAll("t", "1");
			result = Integer.parseInt(instruction, 2);
			result++;
			result = result * -1;
			return result ;
	
		}
		else //num is positive
			return Integer.parseInt(instruction, 2);
		
			}
	
	
	public static void main(String[] args) throws IOException {
//		  String tempPC=Integer.toBinaryString(1024);
//	      String tempPC32 = String.format("%32s", tempPC).replaceAll(" ", "0");  // 32-bit Integer
//	      String tempAddress=Integer.toBinaryString(200);
//	      String tempAddress28 = String.format("%28s", tempAddress).replaceAll(" ", "0");  // 28-bit Integer

		
//	      String y = Integer.toBinaryString(-1601699831);
//	      int hi = converttwoscomplement("10100000100010000000000000001001");
//	      System.out.println(hi);
//	      System.out.println(y);
//	      System.out.println(y2);
		 FilletNeumann f = new FilletNeumann();
		 //f.Memory[0]= 8937472;       //ADD R1 R2 R3
		 //f.Memory[1]= 554737664;   //MUL R2 R4 R5
		// f.start();
//		 System.out.println(f.Memory[0]);
//		 f.translateInstrFile("instructionsFile.txt");	    
//		 String[] content = {"ADD","R1","R2","R3"};
//		 f.test(content);
// 	     // System.out.println(f.Memory[0]);
		 
		 
	
  }
}
