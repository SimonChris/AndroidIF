package thedemitrius.androidif.vm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.zip.DataFormatException;

import thedemitrius.androidif.Init;
import thedemitrius.androidif.glk.GLK;
import thedemitrius.androidif.util.DataTransforms;
import thedemitrius.androidif.util.FastByteBuffer;

public class Glulx implements OPConstants, GLKFunctions
{
	private GLK glk;
	
	// memory
	private FastByteBuffer memory;

	// stack
	private FastByteBuffer stack;

	private int ramstart;
	private int endmem;
	private int ramsize;

	private int pc;

	private int sp;
	private int fp;
	private int lp;
	private int vp;

	private int[] funargs;

	private Random rand;
	
	private int iosys;
	private int iorock;
	
	private HuffmanTree htree;
	
	private byte[] printBuffer;
	private int printPos;
	
	private int[] accelParam=new int[9];
	private AccelStruct[] accel=new AccelStruct[8];
	private int accelCount;
	
	private boolean selecting;
	private boolean running;
	private boolean quitPending;
	
	private static int CLASSES_TABLE=0;
	private static int INDIV_PROP_START=1;
	private static int CLASS_METACLASS=2;
	private static int SELF=6;


	public Glulx(GLK glk) throws IOException
	{
		selecting=false;
		
		this.glk=glk;
		
		printBuffer=new byte[1000];
		printPos=0;
		funargs=new int[32];
		rand=new Random();

		init(getClass().getResourceAsStream(Init.storyURL+"story.ulx"));
		enterFunction(pc, 0);
	}
	
	private void init(InputStream story) throws IOException, OutOfMemoryError
	{
		byte[] buf=new byte[20];
		story.read(buf, 0, 20);
		
		if (buf[0] == 'G' && buf[1] == 'l' && buf[2] == 'u' && buf[3] == 'l')
		{
			int extstart = DataTransforms.getInt(buf, 12);
			endmem=DataTransforms.getInt(buf, 16);
			if (memory==null){
				memory=new FastByteBuffer(endmem);}
			memory.limit(extstart);
			
			System.arraycopy(buf, 0, memory.array(), 0, 20);
			
			int bytesRead=0;
			int startPos=20;
			while (bytesRead!=-1&&startPos< extstart){
				bytesRead=story.read(memory.array(), startPos, extstart -startPos);
				startPos+=bytesRead;}

			memory.clear();
			
			for (int j = extstart; j < endmem; j++)
				memory.put(j, (byte) 0);

			ramstart = memory.getInt(8);
			ramsize=endmem-ramstart;
			int stacksize = memory.getInt(20);
			if (stack==null)
				stack = new FastByteBuffer(stacksize);
			sp = fp = lp = vp = 0;

			pc = memory.getInt(24);
			
			iosys=0;
			iorock=0;
			
			int stringtbl=memory.getInt(28);
			if (stringtbl>0)
				htree=new HuffmanTree(stringtbl);
			
			for (int i=0;i<8;i++){
				accel[i]=new AccelStruct();}
			accelCount=0;
		}
		
		else
		{
			throw new IOException("Not a glulx file.");
		}
	}
	
	public void run()
	{
		quitPending=false;

		try {
			exec();
		} catch (CloneNotSupportedException e) {
			fatal(e.getMessage());
		} catch (InterruptedException e) {
			fatal(e.getMessage());
		} catch (InvocationTargetException e) {
			fatal(e.getMessage());
		} catch (IOException e) {
			fatal(e.getMessage());
		} catch (DataFormatException e) {
			fatal(e.getMessage());
		} catch (ClassNotFoundException e) {
			fatal(e.getMessage());
		}
	}
	
	private void storeOperand(int mode, int addr, int val)
	{
		switch(mode)
		{
		case 0:
			break;
		case 1:
			memory.putInt(addr, val);
			break;
		case 2:
			stack.putInt(addr + lp, val);
			break;
		case 3:
			stack.putInt(sp, val);
			sp += 4;
			break;
		default:
			fatal("storing illegal operand");
		}
	}

	private void storeShortOperand(int mode, int addr, int val)
	{
		short s = (short) val;

		switch(mode)
		{
		case 0:
			break;
		case 1:
			memory.putShort(addr, s);
			break;
		case 2:
			stack.putShort(addr + lp, s);
			break;
		case 3:
			stack.putInt(sp, ((int) s) & 0xffff);
			sp += 4;
			break;
		default:
			fatal("storing illegal operand");
		}
	}

	private void storeByteOperand(int mode, int addr, int val)
	{
		byte b = (byte) val;

		switch(mode)
		{
		case 0:
			break;
		case 1:
			memory.put(addr, b);
			break;
		case 2:
			stack.put(addr + lp, b);
			break;
		case 3:
			stack.putInt(sp, ((int) b) & 0xff);
			sp += 4;
			break;
		default:
			fatal("storing illegal operand");
		}
	}

	private void parseOperands(OP op, int[] modes, int[] values)
	{
		int i;
		int rawmode = 0;
		int arity = op.arity;
		int[] format = op.format;
		int modeaddr = memory.position();

		memory.position(modeaddr + ((arity + 1) / 2));

		for (i = 0; i < arity; i++)
		{
			if ((i & 1) == 0)
			{
				rawmode = (int) memory.get(modeaddr);
				modes[i] = rawmode & 0x0f;
			}
			else
			{
				modes[i] = (rawmode >> 4) & 0x0f;
				modeaddr++;
			}

			if (format[i] == LOAD)
			{
				switch(modes[i])
				{
				case 0:
					values[i] = 0;
					break;
				case 1:
					values[i] = (int) memory.get();
					break;
				case 2:
					values[i] = (int) memory.getShort();
					break;
				case 3:
					values[i] = memory.getInt();
					break;
				case 5:
					values[i] = memory.getInt(((int) memory.get()) & 0xff);
					break;
				case 6:
					values[i] = memory.getInt(((int) memory.getShort()) & 0xffff);
					break;
				case 7:
					values[i] = memory.getInt(memory.getInt());
					break;
				case 8:
					sp = sp - 4;
					values[i] = stack.getInt(sp);
					break;
				case 9:
					values[i] = stack.getInt(lp + (0xff & ((int) memory.get())));
					break;
				case 10:
					values[i] = stack.getInt(lp + (0xffff & ((int) memory.getShort())));
					break;
				case 11:
					values[i] = stack.getInt(lp + memory.getInt());
					break;
				case 13:
					values[i] = memory.getInt((((int) memory.get()) & 0xff) + ramstart);
					break;
				case 14:
					values[i] = memory.getInt((((int) memory.getShort()) & 0xffff) + ramstart);
					break;
				case 15:
					values[i] = memory.getInt(memory.getInt() + ramstart);
					break;
				default:
					fatal("Non-existent addressing mode: " + modes[i]);
				}
			}
			else
			{
				switch(modes[i])
				{
				case 0:
					values[i] = 0;
					break;
				case 8:
					modes[i] = 3;
					values[i] = 0;
					break;
				case 5:
					modes[i] = 1;
					values[i] = ((int) memory.get()) & 0xff;
					break;
				case 9:
					modes[i] = 2;
					values[i] = ((int) memory.get()) & 0xff;
					break;
				case 13:
					modes[i] = 1;
					values[i] = ramstart + (((int) memory.get()) & 0xff);
					break;
				case 6:
					modes[i] = 1;
					values[i] = ((int) memory.getShort()) & 0xffff;
					break;
				case 10:
					modes[i] = 2;
					values[i] = ((int) memory.getShort()) & 0xffff;
					break;
				case 14:
					modes[i] = 1;
					values[i] = ramstart + (((int) memory.getShort()) & 0xffff);
					break;
				case 7:
					modes[i] = 1;
					values[i] = memory.getInt();
					break;
				case 11:
					modes[i] = 2;
					values[i] = memory.getInt();
					break;
				case 15:
					modes[i] = 1;
					values[i] = ramstart + memory.getInt();
					break;
				default:
					fatal("Non-existent addressing mode (store): " + modes[i]);
				}
			}
		}
	}
	
	private void exec() throws CloneNotSupportedException, InterruptedException, InvocationTargetException, IOException, DataFormatException, ClassNotFoundException
	{
		int opcode = 0;
		int val;
		int addr;
		int i;
		int[] values = new int[10];
		int[] modes = new int[10];

		while(running)
		{
            try {
                memory.position(pc);
                opcode = memory.get();

                if ((opcode & 0x80) != 0) {
                    if ((opcode & 0x40) != 0) {
                        opcode &= 0x3f;
                        opcode = (opcode << 8) | memory.get();
                        opcode = (opcode << 8) | memory.get();
                        opcode = (opcode << 8) | memory.get();
                    } else {
                        opcode &= 0x7f;
                        opcode = (opcode << 8) | memory.get();
                    }
                }

                if (opcode < 0)
                    opcode += 512;

                try {
                    if (OP.OPS[opcode].format != null)
                        parseOperands(OP.OPS[opcode], modes, values);
                } catch (NullPointerException e) {
                    fatal("Unimplemented Glulx opcode: " + opcode);
                    return;
                }
                pc = memory.position();


                switch (opcode) {
                    case NOP:
                        break;
                    case ADD:
                        storeOperand(modes[2], values[2], values[0] + values[1]);
                        break;
                    case SUB:
                        storeOperand(modes[2], values[2], values[0] - values[1]);
                        break;
                    case MUL:
                        storeOperand(modes[2], values[2], values[0] * values[1]);
                        break;
                    case DIV:
                        if (values[1] == 0)
                            fatal("Division by zero.");
                        storeOperand(modes[2], values[2], values[0] / values[1]);
                        break;
                    case MOD:
                        if (values[1] == 0)
                            fatal("Mod by zero.");
                        storeOperand(modes[2], values[2], values[0] % values[1]);
                        break;
                    case NEG:
                        storeOperand(modes[1], values[1], 0 - values[0]);
                        break;
                    case BITAND:
                        storeOperand(modes[2], values[2], values[0] & values[1]);
                        break;
                    case BITOR:
                        storeOperand(modes[2], values[2], values[0] | values[1]);
                        break;
                    case BITXOR:
                        storeOperand(modes[2], values[2], values[0] ^ values[1]);
                        break;
                    case BITNOT:
                        storeOperand(modes[1], values[1], ~values[0]);
                        break;
                    case SHIFTL:
                        val = ((values[1] & 0xff) > 31) ? 0 : values[0] << (values[1] & 0xff);
                        storeOperand(modes[2], values[2], val);
                        break;
                    case USHIFTR:
                        val = ((values[1] & 0xff) > 31) ? 0 : values[0] >>> (values[1] & 0xff);
                        storeOperand(modes[2], values[2], val);
                        break;
                    case SSHIFTR:
                        if (values[1] >= 0 && values[1] < 32)
                            val = ((values[1] & 0xff) > 31) ? 0 : values[0] >> (values[1] & 0xff);
                        else if (values[0] < 0)
                            val = -1;
                        else
                            val = 0;
                        storeOperand(modes[2], values[2], val);
                        break;
                    case JUMP:
                        handleRelativeJump(values[0]);
                        break;
                    case JZ:
                        if (values[0] == 0)
                            handleRelativeJump(values[1]);
                        break;
                    case JNZ:
                        if (values[0] != 0)
                            handleRelativeJump(values[1]);
                        break;
                    case JEQ:
                        if (values[0] == values[1])
                            handleRelativeJump(values[2]);
                        break;
                    case JNE:
                        if (values[0] != values[1])
                            handleRelativeJump(values[2]);
                        break;
                    case JLT:
                        if (values[0] < values[1])
                            handleRelativeJump(values[2]);
                        break;
                    case JLE:
                        if (values[0] <= values[1])
                            handleRelativeJump(values[2]);
                        break;
                    case JGT:
                        if (values[0] > values[1])
                            handleRelativeJump(values[2]);
                        break;
                    case JGE:
                        if (values[0] >= values[1])
                            handleRelativeJump(values[2]);
                        break;
                    case JLTU:
                        if (((long) values[0] & 0xffffffffL) < ((long) values[1] & 0xffffffffL))
                            handleRelativeJump(values[2]);
                        break;
                    case JLEU:
                        if (((long) values[0] & 0xffffffffL) <= ((long) values[1] & 0xffffffffL))
                            handleRelativeJump(values[2]);
                        break;
                    case JGTU:
                        if (((long) values[0] & 0xffffffffL) > ((long) values[1] & 0xffffffffL))
                            handleRelativeJump(values[2]);
                        break;
                    case JGEU:
                        if (((long) values[0] & 0xffffffffL) >= ((long) values[1] & 0xffffffffL))
                            handleRelativeJump(values[2]);
                        break;
                    case JUMPABS:
                        pc = values[0];
                        break;
                    case COPY:
                        storeOperand(modes[1], values[1], values[0]);
                        break;
                    case COPYS:
                        val = (modes[0] == 0x03 || modes[0] == 0x08) ? values[0] : (values[0] >>> 16);
                        storeShortOperand(modes[1], values[1], val);
                        break;
                    case COPYB:
                        val = (modes[0] == 0x03 || modes[0] == 0x08) ? values[0] : (values[0] >>> 24);
                        storeByteOperand(modes[1], values[1], val);
                        break;
                    case SEXS:
                        val = ((values[0] & 0x8000) != 0)
                                ? (values[0] | 0xffff0000) : (values[0] & 0x0000ffff);
                        storeOperand(modes[1], values[1], val);
                        break;
                    case SEXB:
                        val = ((values[0] & 0x80) != 0)
                                ? (values[0] | 0xffffff00) : (values[0] & 0x000000ff);
                        storeOperand(modes[1], values[1], val);
                        break;
                    case ALOAD:
                        storeOperand(modes[2], values[2],
                                memory.getInt(values[0] + (4 * values[1])));
                        break;
                    case ALOADS:
                        storeOperand(modes[2], values[2],
                                ((int) memory.getShort(values[0] + (2 * values[1]))) & 0xffff);
                        break;
                    case ALOADB:
                        storeOperand(modes[2], values[2],
                                ((int) memory.get(values[0] + values[1])) & 0xff);
                        break;
                    case ALOADBIT:
                        addr = values[0] + (values[1] / 8);
                        val = values[1] % 8;

                        if (val < 0) {
                            addr--;
                            val += 8;
                        }

                        storeOperand(modes[2], values[2],
                                ((memory.get(addr) & (byte) (1 << val)) != 0) ? 1 : 0);
                        break;
                    case ASTORE:
                        storeOperand(1, values[0] + (4 * values[1]), values[2]);
                        break;
                    case ASTORES:
                        storeShortOperand(1, values[0] + (2 * values[1]), values[2]);
                        break;
                    case ASTOREB:
                        storeByteOperand(1, values[0] + values[1], values[2]);
                        break;
                    case ASTOREBIT:
                        addr = values[0] + (values[1] / 8);
                        val = values[1] % 8;

                        if (val < 0) {
                            addr--;
                            val += 8;
                        }

                        if (values[2] == 0)
                            memory.put(addr, (byte) (memory.get(addr) & ((byte) ~(1 << val))));
                        else
                            memory.put(addr, (byte) (memory.get(addr) | (byte) (1 << val)));
                        break;
                    case STKCOUNT:
                        storeOperand(modes[0], values[0], (sp - vp) / 4);
                        break;
                    case STKPEEK:
                        if (values[0] < 0 || values[0] >= ((sp - vp) / 4))
                            fatal("stkpeek: outside valid stack range");

                        storeOperand(modes[1], values[1],
                                stack.getInt(sp - (4 * (values[0] + 1))));
                        break;
                    case STKSWAP:
                        if (sp - vp < 8)
                            fatal("Must be at least two values on the stack to execute stkswap.");

                        val = stack.getInt(sp - 4);
                        addr = stack.getInt(sp - 8);
                        stack.putInt(sp - 8, val);
                        stack.putInt(sp - 4, addr);
                        break;
                    case STKCOPY:
                        if (sp - vp < 4 * values[0])
                            fatal("Cannot copy " + values[0] + " stack items.  Stack too small.");
                        for (i = 0; i < values[0]; i++)
                            stack.putInt(sp + (4 * i), stack.getInt(sp - (4 * (values[0] - i))));
                        sp += 4 * values[0];
                        break;
                    case STKROLL:
                        if (values[0] < 0)
                            fatal("Cannot roll negative number of stack entries.");
                        if (((sp - vp) / 4) < values[0])
                            fatal("Cannot roll more stack values than there are on the stack.");

				/* Algorithm thanks to Andrew Plotkin... */

                        if (values[0] == 0)
                            break;

                        if (values[1] > 0)
                            val = values[0] - (values[1] % values[0]);
                        else
                            val = (-values[1]) % values[0];

                        if (val == 0)
                            break;

                        addr = sp - (4 * values[0]);
                        for (i = 0; i < val; i++)
                            stack.putInt(sp + (4 * i), stack.getInt(addr + (4 * i)));
                        for (i = 0; i < values[0]; i++)
                            stack.putInt(addr + (4 * i), stack.getInt(addr + (4 * (val + i))));
                        break;
                    case CALL:
                        popArguments(values[1]);
                        pushCallstub(modes[2], values[2]);
                        enterFunction(values[0], values[1]);
                        break;
                    case RETURN:
                        leaveFunction();
                        if (sp == 0)
                            running = false;
                        else
                            popCallstub(values[0]);
                        break;
                    case TAILCALL:
                        popArguments(values[1]);
                        leaveFunction();
                        enterFunction(values[0], values[1]);
                        break;
                    case CATCH:
                        pushCallstub(modes[0], values[0]);
                        storeOperand(modes[0], values[0], sp);
                        handleRelativeJump(values[1]);
                        break;
                    case THROW:
                        sp = values[1];
                        popCallstub(values[0]);
                        break;
                    case STREAMCHAR:
                        if (iosys == 2) {
                            glk.putChar((byte) values[0]);
                        } else {
                            if (iosys == 1) {
                                pushCallstub(0, 0);
                                funargs[0] = values[0];
                                enterFunction(iorock, 1);
                            }
						}
						break;
                    case STREAMNUM:
                        glk.putString(stringToBytes(Integer.toString(values[0])), 0);
                        break;
                    case STREAMSTR:
                        streamString(values[0], 0, 0);
                        break;
                    case STREAMUNICHAR:
                        if (iosys == 2) {
                            glk.putCharUni(values[0]);
                        } else {
                            if (iosys == 1) {
                                pushCallstub(0, 0);
                                funargs[0] = values[0];
                                enterFunction(iorock, 1);
                            }
						}
						break;
                    case OPConstants.GESTALT:
                        storeOperand(modes[2], values[2], gestalt(values[0], values[1]));
                        break;
                    case DEBUGTRAP:
                        fatal("debugtrap executed.");
                        break;
                    case CALLF:
                        pushCallstub(modes[1], values[1]);
                        enterFunction(values[0], 0);
                        break;
                    case CALLFI:
                        if (accel[0].address == values[0]) {
                            storeOperand(modes[2], values[2], I_zregion(values[1]));
                        } else {
                            if (accel[7].address == values[0]) {
                                storeOperand(modes[2], values[2], (int) Math.pow(2, memory.get(values[1])));
                            } else {
                                funargs[0] = values[1];
                                pushCallstub(modes[2], values[2]);
                                enterFunction(values[0], 1);
                            }
						}
						break;
                    case CALLFII:
                        if (accel[1].address == values[0]) {
                            storeOperand(modes[3], values[3], I_cptab(values[1], (short) values[2]));
                        } else {
                            if (accel[2].address == values[0]) {
                                storeOperand(modes[3], values[3], I_rapr(values[1], values[2]));
                            } else {
                                if (accel[3].address == values[0]) {
                                    storeOperand(modes[3], values[3], I_rlpr(values[1], values[2]));
                                } else {
                                    if (accel[4].address == values[0]) {
                                        storeOperand(modes[3], values[3], I_occl(values[1], values[2]));
                                    } else {
                                        if (accel[5].address == values[0]) {
                                            storeOperand(modes[3], values[3], I_rvpr(values[1], values[2]));
                                        } else {
                                            if (accel[6].address == values[0]) {
                                                storeOperand(modes[3], values[3], I_oppr(values[1], values[2]));
                                            } else {
                                                funargs[0] = values[1];
                                                funargs[1] = values[2];
                                                pushCallstub(modes[3], values[3]);
                                                enterFunction(values[0], 2);
                                            }
										}
									}
								}
							}
						}
						break;
                    case CALLFIII:
                        funargs[0] = values[1];
                        funargs[1] = values[2];
                        funargs[2] = values[3];
                        pushCallstub(modes[4], values[4]);
                        enterFunction(values[0], 3);
                        break;
                    case GETMEMSIZE:
                        storeOperand(modes[0], values[0], endmem);
                        break;
                    case SETMEMSIZE:
                        storeOperand(modes[1], values[1], setMemSize(values[0]));
                        break;
                    case GETSTRINGTBL:
                        if (htree != null)
                            storeOperand(modes[0], values[0], htree.startaddr);
                        else
                            storeOperand(modes[0], values[0], 0);
                        break;
                    case SETSTRINGTBL:
                        htree = new HuffmanTree(values[0]);
                        break;
                    case GETIOSYS:
                        storeOperand(modes[0], values[0], iosys);
                        storeOperand(modes[1], values[1], iorock);
                        break;
                    case SETIOSYS:
                        setIOSys(values[0], values[1]);
                        break;
                    case RANDOM:
                        if (values[0] == 0)
                            storeOperand(modes[1], values[1], rand.nextInt());
                        else if (values[0] < 0)
                            storeOperand(modes[1], values[1], 0 - rand.nextInt(0 - values[0]));
                        else
                            storeOperand(modes[1], values[1], rand.nextInt(values[0]));
                        break;
                    case SETRANDOM:
                        rand.setSeed((long) values[0]);
                        break;
                    case LINEARSEARCH:
                        val = linearSearch(values[0], values[1], values[2], values[3], values[4],
                                values[5], values[6]);
                        storeOperand(modes[7], values[7], val);
                        break;
                    case BINARYSEARCH:
                        val = binarySearch(values[0], values[1], values[2], values[3], values[4],
                                values[5], values[6]);
                        storeOperand(modes[7], values[7], val);
                        break;
                    case LINKEDSEARCH:
                        val = linkedSearch(values[0], values[1], values[2], values[3], values[4],
                                values[5]);
                        storeOperand(modes[6], values[6], val);
                        break;
                    case QUIT:
                        quit();
                        break;
                    case VERIFY:
                        storeOperand(modes[0], values[0], verify(false));
                        break;
                    case RESTART:
                        restartGame();
                        break;
                    case SAVE:
                        pushCallstub(modes[1], values[1]);
                        val = save(values[0]);
                        popCallstub(val);
                        break;
                    case RESTORE:
                        val = restore(values[0]);
                        if (val == 0)
                            popCallstub(-1);
                        else
                            storeOperand(modes[1], values[1], val);
                        break;
                    case SAVEUNDO:
                        storeOperand(modes[0], values[0], 1);
                        break;
                    case RESTOREUNDO:
                        storeOperand(modes[0], values[0], 1);
                        break;
                    case PROTECT:
                        break;
                    case GLK:
                        popArguments(values[1]);
                        val = glkCall(values[0], values[1]);
                        storeOperand(modes[2], values[2], val);
                        break;
                    case ACCELFUNC:
                        accel[accelCount].funcNum = values[0];
                        accel[accelCount].address = values[1];
                        accelCount++;
                        break;
                    case ACCELPARAM:
                        accelParam[values[0]] = values[1];
                        break;
                    default:
                        fatal("Unknown Glulx Opcode: " + opcode);
                }
            }
            catch (Exception e)
            {
                restartGame();
            }
		}
	}
	
	private void restartGame()
	{
		glk.toggleKeyboardFromThread(false);
		glk.clearTextBuffer();
		glk.clearTextJumps();
		glk.clearFiles();
		try {
			init(getClass().getResourceAsStream(Init.storyURL+"story.ulx"));
		} catch (IOException eRestart) {
			System.err.println(eRestart);
			fatal("Could not restart!");
		}
		enterFunction(pc, 0);
	}
	
	private void enterFunction(int addr,int numargs)
	{
		int ltype, lnum;
		int format, local;
		int i, j;
		int len = 0;
		int funtype = ((int) memory.get(addr++)) & 0xff;

		if (funtype != 0xc0 && funtype != 0xc1)
		{
			if (funtype >= 0xc0 && funtype <= 0xdf)
				fatal("Unknown type of function.");
			else
				fatal("Attempt to call non-function.");
		}
		fp = sp;
		i = 0;
		while (true)
		{
			ltype = ((int) memory.get(addr++)) & 0xff;
			lnum = ((int) memory.get(addr++)) & 0xff;
			stack.put(fp + 8 + (2 * i), (byte) ltype);
			stack.put(fp + 8 + (2 * i) + 1, (byte) lnum);
			i++;

			if (ltype == 0)
			{
				if ((i & 1) != 0)
				{
					stack.put(fp + 8 + (2 * i), (byte) 0);
					stack.put(fp + 8 + (2 * i) + 1, (byte) 0);
					i++;
				}
				break;
			}

			if (ltype == 4)
			{
				while ((len & 3) != 0)
					len++;
			}
			else if (ltype == 2)
			{
				while ((len & 1) != 0)
					len++;
			}

			len += ltype * lnum;
		}

		while ((len & 3) != 0)
			len++;

		lp = fp + 8 + (2 * i);
		vp = lp + len;

		stack.putInt(fp, 8 + (2 * i) + len);
		stack.putInt(fp + 4, 8 + (2 * i));

		sp = vp;
		pc = addr;

		for (j = 0; j < len; j++)
			stack.put(lp + j, (byte) 0);

		if (funtype == 0xc0)
		{
			for (j = numargs - 1; j >=0; j--)
			{
				stack.putInt(sp, funargs[j]);
				sp += 4;
			}
			stack.putInt(sp, numargs);
			sp += 4;
		}
		else
		{
			format = fp + 8;
			local = lp;
			i = 0;
			while (i < numargs)
			{
				ltype = ((int) stack.get(format++)) & 0xff;
				lnum = ((int) stack.get(format++)) & 0xff;
				if (ltype == 0)
					break;
				if (ltype == 4)
				{
					while ((local & 3) != 0)
						local++;
					while (i < numargs && lnum != 0)
					{
						stack.putInt(local, funargs[i++]);
						local += 4;
						lnum--;
					}
				}
				else if (ltype == 2)
				{
					while ((local & 1) != 0)
						local++;
					while (i < numargs && lnum != 0)
					{
						stack.putShort(local, (short) (funargs[i++] & 0xffff));
						local += 2;
						lnum--;
					}
				}
				else
				{
					while (i < numargs && lnum != 0)
					{
						stack.put(local, (byte) (funargs[i++] & 0xff));
						local++;
						lnum--;
					}
				}
			}
		}
	}

	private void quit()
	{
		running = false;
		synchronized (this){
			notify();}
	}

	private int verify(boolean progress)
	{
		return (0);
	}

	private int linkedSearch(int key, int keySize, int start, 
			int keyOffset, int nextOffset, int options)
	{
		byte curKeyByte;
		int curKey;
		boolean found;
		boolean zeroKey;
		int nextAddr;
		FastByteBuffer b = memory;
		boolean zeroTerm = ((options & 0x02) != 0);

		if (keySize < 1 || start < 0)
			fatal("Illegal argument(s) to linkedsearch.");

		while (true)
		{
			found = true;
			zeroKey = true;

			if ((options & 0x01) != 0)
			{
				for (int j = 0; j < keySize; j++)
				{
					curKeyByte = b.get(start + keyOffset + j);
					found = (curKeyByte == b.get(key + j));
					if (curKeyByte != (byte) 0)
						zeroKey = false;

					if (found)
						break;
				}
			}
			else
			{
				switch(keySize)
				{
				case 1:
					curKey = ((int) b.get(start + keyOffset)) & 0xff;
					found = ((key & 0xff) == curKey);
					zeroKey = (curKey == 0);
					break;
				case 2:
					curKey = ((int) b.getShort(start + keyOffset)) & 0xffff;
					found = ((key & 0xffff) == curKey);
					zeroKey = (curKey == 0);
					break;
				case 3:
					curKey = (b.getInt(start + keyOffset) >>> 8);
					found = ((key & 0xffffff) == curKey);
					zeroKey = (curKey == 0);
				case 4:
					curKey = b.getInt(start + keyOffset);
					found = (key == curKey);
					break;
				default:
					fatal("Illegal key size for direct linkedsearch.");
				}
			}

			if (found || (zeroTerm && zeroKey))
				break;

			nextAddr = b.getInt(start + nextOffset);
			if (nextAddr == 0)
				break;

			start = nextAddr;
		}

		if (found)
			return start;
		else
			return 0;
	}


	private int binarySearch(int key, int keySize, int start, 
			int structSize, int numStructs, 
			int keyOffset, int options)
	{
		int curKeyByte;
		int curKey;
		int curStart = start;
		int diff = -1;
		int i = 0;
		int bottom = 0;
		int top = numStructs;
		FastByteBuffer b = memory;

		if (keySize < 1 || start < 0 || structSize < 1 || numStructs < 0)
			fatal("Illegal argument(s) to binarysearch.");

		while ((top - bottom) > 0)
		{
			diff = 0;
			i = bottom + ((top - bottom) / 2);
			curStart = start + (i * structSize);

			if ((options & 0x01) != 0)
			{
				for (int j = 0; j < keySize; j++)
				{
					curKeyByte = ((int) b.get(curStart + keyOffset + j)) & 0xff;
					diff = (((int) b.get(key + j)) & 0xff) - curKeyByte;
					if (diff != 0)
						break;
				}
			}
			else
			{
				switch(keySize)
				{
				case 1:
					curKey = ((int) b.get(curStart + keyOffset)) & 0xff;
					diff = (key & 0xff) - curKey;
					break;
				case 2:
					curKey = ((int) b.getShort(curStart + keyOffset)) & 0xffff;
					diff = (key & 0xffff) - curKey;
					break;
				case 3:
					curKey = (b.getInt(curStart + keyOffset) >>> 8);
					diff = (key & 0xffffff) - curKey;
				case 4:
					curKey = b.getInt(curStart + keyOffset);
					diff = key - curKey;
					break;
				default:
					fatal("Illegal key size for direct binarysearch.");
				}
			}

			if (diff == 0)
				break;

			if (diff < 0)
				top = i;
			else
				bottom = i + 1;
		}

		if (diff == 0)
		{
			if ((options & 0x04) == 0)
				return curStart;
			else
				return i;
		}
		else
		{
			if ((options & 0x04) == 0)
				return 0;
			else
				return -1;
		}
	}

	private int linearSearch(int key, int keySize, int start, 
			int structSize, int numStructs, 
			int keyOffset, int options)
	{
		int i = 0;
		int curKey;
		byte curKeyByte;
		boolean found = false;
		boolean zeroKey = false;
		boolean zeroTerm = ((options & 0x02) != 0);
		FastByteBuffer b = memory;

		if (keySize < 1 || start < 0 || structSize < 1)
			fatal("Illegal argument(s) to linearseach.");

		while (numStructs < 0 || i < numStructs)
		{
			found = true;
			zeroKey = true;
			if ((options & 0x01) != 0)
			{
				for (int j = 0; j < keySize; j++)
				{
					curKeyByte = b.get(start + keyOffset + j);
					if (curKeyByte != (byte) 0)
						zeroKey = false;

					if (curKeyByte != b.get(key + j))
					{
						found = false;
						break;
					}
				}
			}
			else
			{
				switch(keySize)
				{
				case 1:
					curKey = ((int) b.get(start + keyOffset)) & 0xff;
					found = ((key & 0xff) == curKey);
					zeroKey = (curKey == 0);
					break;
				case 2:
					curKey = ((int) b.getShort(start + keyOffset)) & 0xffff;
					found = ((key & 0xffff) == curKey);
					zeroKey = (curKey == 0);
					break;
				case 3:
					curKey = (b.getInt(start + keyOffset) >>> 8);
					found = ((key & 0xffffff) == curKey);
					zeroKey = (curKey == 0);
				case 4:
					curKey = b.getInt(start + keyOffset);
					found = (key == curKey);
					break;
				default:
					fatal("Illegal key size for direct linearsearch.");
				}
			}

			if (found || (zeroTerm && zeroKey))
				break;

			i++;
			start += structSize;
		}

		if (found)
		{
			if ((options & 0x04) != 0)
				return i;
			else
				return start;
		}
		else if (zeroTerm && zeroKey)
		{
			if ((options & 0x04) != 0)
				return -1;
			else
				return 0;
		}
		else
		{
			return 0;
		}
	}

	private int setMemSize(int newsize)
	{
		FastByteBuffer newmem;
		int origsize;

		if (newsize == endmem)
			return 0;

		if ((newsize & 0xff) == 0)
		{
			origsize = memory.getInt(16);
			if (newsize >= origsize)
			{
				newmem = new FastByteBuffer(newsize);
				memory.position(0);
				newmem.put(memory);
				for (int i = memory.limit(); i < newsize; i++)
					newmem.put((byte) 0);
				memory = newmem;

				endmem = newsize;
				return 0;
			}
		}
		return 1;
	}

	private int gestalt(int a, int b)
	{
		switch(a)
		{
		case 0:
			return 0x00020000;
		case 1:
			return 0x00001000;
		case 2:
			return 1;
		case 3:
			return 0;
		case 4:
			switch(b)
			{
			case 0:
			case 1:
			case 2:
				return 1;
			default:
				return 0;
			}
		case 9:
			return (1);
		case 10:
			if (b<9){
				return (1);}
			return (0);
		case 100:
			return 1;
		default:
			return 0;
		}
	}

	private void popArguments(int numargs)
	{
		if (sp < (vp + (4 * numargs)))
			fatal("Attempting to pop too many [" + numargs +
			                                   "] function arguments.  sp=" + sp + "; vp=" + vp);

		if (numargs > funargs.length)
			funargs = new int[numargs];

		for (int i = 0; i < numargs; i++)
		{
			sp -= 4;
			funargs[i] = stack.getInt(sp);
		}
	}

	private void pushCallstub(int mode, int addr)
	{
		stack.putInt(sp, mode);
		stack.putInt(sp + 4, addr);
		stack.putInt(sp + 8, pc);
		stack.putInt(sp + 12, fp);
		sp += 16;
	}

	private void popCallstub(int retval) throws IOException
	{
		int dtype, daddr;

		sp -= 16;
		fp = stack.getInt(sp + 12);
		pc = stack.getInt(sp + 8);
		daddr = stack.getInt(sp + 4);
		dtype = stack.getInt(sp);

		vp = fp + stack.getInt(fp);
		lp = fp + stack.getInt(fp + 4);

		if (sp < vp)
			fatal("while popping callstub, sp=" + sp + "; vp=" + vp);

		switch(dtype)
		{
		case 0x10:
			streamString(pc, 2, daddr);
			break;
		case 0x11:
			fatal("String terminator callstub found at end of function call.");
			break;
		case 0x12:
			fatal("Unknown dtype 0x12 in popCallStub.");
			break;
		case 0x13:
			streamString(pc, 1, daddr);
			break;
		default:
			storeOperand(dtype, daddr, retval);
		}
	}

	private StringCallResult popCallstubString()
	{
		StringCallResult r = new StringCallResult();
		int desttype, destaddr, newpc;

		sp -= 16;
		desttype = stack.getInt(sp);
		destaddr = stack.getInt(sp + 4);
		newpc = stack.getInt(sp + 8);

		pc = newpc;

		if (desttype == 0x11)
		{
			r.pc = 0;
			r.bitnum = 0;
			return r;
		}

		if (desttype == 0x10)
		{
			r.pc = pc;
			r.bitnum = destaddr;
			return r;
		}

		fatal("Function terminator call stub at the end of a string.");
		return null;
	}
	
	private void leaveFunction()
	{
		sp = fp;
	}

	private void handleRelativeJump(int offset) throws IOException
	{
		if (offset == 0 || offset == 1)
		{
			leaveFunction();
			if (sp == 0)
			{
				running = false;
				return;
			}
			popCallstub(offset);
		}
		else
		{
			pc = pc + offset - 2;
		}
	}
	
	private static class StringCallResult
	{
		int pc;
		int bitnum;
	}

	private void setIOSys(int newsys,int newrock)
	{
		switch(newsys){
		case 1:
			iosys=1;
			iorock=newrock;
			break;
		case 2:
			iosys=2;
			iorock=0;
			break;
		default:
			iosys=0;
			iorock=0;}
	}
	
	void streamString(int addr, int inmiddle, int bit) throws IOException
	{
		int ch;
		int oaddr;
		int type;
		boolean alldone = false;
		boolean started = (inmiddle != 0);

		while (!alldone)
		{
			if (inmiddle == 0)
			{
				type = (int) memory.get(addr++) & 0xff;
				bit = 0;
			}
			else
			{
				if (inmiddle == 1)
					type = 0xe0;
				else
					type = 0xe1;
			}

			if (type == 0xe1)
			{
				if (htree == null)
					fatal("Attempt to stream a compressed string with no Huffman table.");
				HuffmanTree.Node troot = htree.root;
				HuffmanTree.Node n;
				int done = 0;

				if (troot == null)
					troot = htree.readTree(memory.getInt(htree.startaddr + 8), 
							false);

				n = troot;

				while (done == 0)
				{
					switch(n.type)
					{
					case 0x00:
						boolean on;
						byte b = memory.get(addr);

						if (bit > 0)
							b >>= bit;
							on = ((b & 1) != 0);

							if (++bit > 7)
							{
								bit = 0;
								addr++;
							}

							if (on)
								n = (n.right == null) 
								? (n.right = htree.readTree(n.rightaddr, false))
										: n.right;
								else
									n = (n.left == null)
									? (n.left = htree.readTree(n.leftaddr, false))
											: n.left;
									break;
					case 0x01:
						done = 1;
						break;
					case 0x02:
						switch(iosys)
						{
						case 2:
							printChar((byte)n.c);
							break;
						case 1:
							if (!started)
							{
								pushCallstub(0x11, 0);
								started = true;
							}
							pc = addr;
							pushCallstub(0x10, bit);
							funargs[0]= n.c &0xff;
							enterFunction(iorock, 1);
							return;
						default:
							break;
						}
						n = troot;
						break;
					case 0x03:
						switch(iosys)
						{
						case 2:
							memory.position(n.addr);
							printBuffer(n.addr,n.numargs);
							n = troot;
							break;
						case 1:
							if (!started)
							{
								pushCallstub(0x11, 0);
								started = true;
							}
							pc = addr;
							pushCallstub(0x10, bit);
							inmiddle = 1;
							addr = n.addr;
							done = 2;
							break;
						default:
							n = troot;
						}
						break;
					case 0x04:
						switch(iosys)
						{
						case 2:
							printChar(n.c);
							break;
						case 1:
							if (!started)
							{
								pushCallstub(0x11, 0);
								started = true;
							}
							pc = addr;
							pushCallstub(0x10, bit);
							funargs[0]=n.c;
							enterFunction(iorock, 1);
							return;
						default:
							break;
						}
						n = troot;
						break;
					case 0x08:
					case 0x09:
					case 0x0a:
					case 0x0b:
						int otype;
						oaddr = n.addr;
						if (n.type == 0x09 || n.type == 0x0b)
							oaddr = memory.getInt(oaddr);
						otype = (int) memory.get(oaddr) & 0xff;

						if (!started)
						{
							pushCallstub(0x11, 0);
							started = true;
						}
						if (otype >= 0xe0 && otype <= 0xff)
						{
							pc = addr;
							pushCallstub(0x10, bit);
							inmiddle = 0;
							addr = oaddr;
							done = 2;
						}
						else if (otype >= 0xc0 && otype <= 0xdf)
						{
							pc = addr;
							pushCallstub(0x10, bit);
							System.arraycopy(n.args, 0, funargs, 0, n.numargs);
							enterFunction(oaddr, n.numargs);
							return;
						}
						else
						{
							fatal("Attempting indirect reference to unknown object while " +
									"decoding string.");
						}
						break;
					default:
						fatal("Unknown node type in cached Huffman tree.");
					}
				}

				if (done > 1)
					continue;

			}
			else if (type == 0xe0)
			{
				switch(iosys)
				{
				case 2:
					while ((ch = memory.get(addr++)) != 0)
						printChar((byte)ch);
					break;
				case 1:
					if (!started)
					{
						pushCallstub(0x11, 0);
						started = true;
					}
					ch = memory.get(addr++);
					if (ch != 0)
					{
						pc = addr;
						pushCallstub(0x13, 0);
						funargs[0]= ch & 0xff;
						enterFunction(iorock, 1);
						return;
					}
					break;
				default:
				}
			}
			else if (type == 0xe2)
			{
				switch(iosys)
				{
				case 2:
					addr+=3;
					while ((ch = memory.getInt(addr+=4)) != 0)
						printChar(ch);
					break;
				case 1:
					if (!started)
					{
						pushCallstub(0x11, 0);
						started = true;
					}
					ch = memory.getInt(addr+=4);
					if (ch != 0)
					{
						pc = addr;
						pushCallstub(0x13, 0);
						funargs[0]=ch;
						enterFunction(iorock, 1);
						return;
					}
					break;
				default:
				}
			}
			else if (type >= 0xe0 && type <= 0xff)
			{
				fatal("Attempt to print unknown type of string.");
			}
			else
			{
				fatal("Attempt to print non-string.");
			}

			if (!started)
			{
				alldone = true;
			}
			else
			{
				StringCallResult r = popCallstubString();
				if (r.pc == 0)
				{
					alldone = true;
				}
				else
				{
					addr = r.pc;
					bit = r.bitnum;
					inmiddle = 2;
				}
			}
		}
		
		if (printPos>0){
			glk.putBuffer(printBuffer, 0, printPos);
			printPos=0;}
	}
	
	private byte[] stringToBytes(String s)
	{
		byte[] result=new byte[s.length()+1];
		
		for (int i=0;i<s.length();i++){
			result[i]=(byte) s.charAt(i);}

		result[s.length()]=0;
		
		return (result);
	}
	
	private void printChar(int ch) throws IOException
	{
		if (ch<0){
			if (printPos>0){
				glk.putBuffer(printBuffer, 0, printPos);
				printPos=0;}

			glk.putCharUni(ch+256);
			return;}

		if (printPos>=1000){
			glk.putBuffer(printBuffer, 0, printPos);
			printPos=0;}

		printBuffer[printPos]=(byte)ch;
		printPos++;
	}
	
	private void printBuffer(int startPos,int len) throws IOException
	{
		if ((printPos+len)>=1000){
			glk.putBuffer(printBuffer, 0, printPos);
			printPos=0;}

		if (len>1000){
			fatal("Tried to print an array of "+len+" characters using printBuffer.");
			return;}

		System.arraycopy(memory.array(), startPos, printBuffer, printPos, len);
	}
	
	public int save(int streamId) throws IOException
	{
		glk.saveTextJumps(streamId);
		glk.saveTextBuffer(streamId);
		
		glk.writeInt(streamId, pc);
		glk.putBufferStream(streamId, memory.array(), ramstart, ramsize);
		
		glk.writeInt(streamId, sp);
		glk.putBufferStream(streamId, stack.array(), 0, sp);
		
		return 0;
	}
	
	private int restore(int in) throws IOException, DataFormatException
	{
		glk.restoreTextJumps(in);
		glk.loadTextBuffer(in);
		
		pc=glk.readInt(in);
		glk.getBufferStream(in, memory.array(), ramstart, ramsize);
		
		sp=glk.readInt(in);
		glk.getBufferStream(in, stack.array(), 0, sp);
		
		glk.streamClose(in, null, -1);
		
		return (0);
	}
	
	private boolean I_objinclass(int obj)
	{
		int NUM_ATTR_BYTES = 7;
		return (memory.getInt(obj+13+accelParam[NUM_ATTR_BYTES])==accelParam[CLASS_METACLASS]);
	}

	private int I_zregion(int addr)
	{
		if (addr<36||addr>=endmem)
			return (0);

		int val=memory.get(addr);

		if (val<0)
			val=256+val;

		if (val>=0xE0)
			return (3);

		if (val>=0xc0)
			return (2);

		if (val>=0x70&&val<=0x7f&&addr>=ramstart)
			return (1);

		return (0);
	}

	private int I_cptab(int obj,short id)
	{
		if (I_zregion(obj)!=1){

			fatal("Tried to find the ~.~ of something in CP_Tab accelerated function.");
			return (0);}

		int start=memory.getInt(obj+16);
			if (start==0)
				return (0);

			int left=0;
			int right=memory.getInt(start)-1;
			start+=4;

			while (left<=right){
				int midpt=(left+right)/2;
				int address=start+(midpt*10);
				short chk=memory.getShort(address);
				if (id==chk)
					return (address);
				if (id>chk)
					left=midpt+1;
				else
					right=midpt-1;}

		return (0);
	}

	private int I_rapr(int obj,int id)
	{
		int cla=0;

		if ((id&0xFFFF0000)!=0){
			cla=memory.getInt(accelParam[CLASSES_TABLE]+((id&0xFFFF)*4));
			if (I_occl(obj,cla)==0)
				return (0);
			id=id>>16;
			obj=cla;}

		int prop=I_cptab(obj,(short) id);
			if (prop==0)
				return (0);

			if (I_objinclass(obj)&&cla==0){
				if (id<accelParam[INDIV_PROP_START]||id>=accelParam[INDIV_PROP_START]+8)
					return (0);}

		if (memory.getInt(accelParam[SELF])!=obj){
						if ((memory.get(prop+9)&1)!=0)
							return (0);}

		return (memory.getInt(prop+4));
	}

	private int I_rlpr(int obj,int id)
	{
		int cla=0;

		if ((id&0xFFFF0000)!=0){
			cla=memory.getInt(accelParam[CLASSES_TABLE]+((id&0xFFFF)*4));
			if (I_occl(obj,cla)==0)
				return (0);
			id=id>>16;
			obj=cla;}

		int prop=I_cptab(obj,(short) id);
			if (prop==0)
				return (0);

			if (I_objinclass(obj)&&cla==0){
				if (id<accelParam[INDIV_PROP_START]||id>=accelParam[INDIV_PROP_START]+8)
					return (0);}

		if (memory.getInt(accelParam[SELF])!=obj){
						if ((memory.get(prop+9)&1)!=0)
							return (0);}

		return (4*memory.getShort(prop+2));
	}

	private int I_occl(int obj,int cla)
	{
		int zr=I_zregion(obj);

		int STRING_METACLASS = 5;
		if (zr==3){
			if (cla==accelParam[STRING_METACLASS])
				return (1);
			return (0);}

		int ROUTINE_METACLASS = 4;
		if (zr==2){
				if (cla==accelParam[ROUTINE_METACLASS])
					return (1);
				return (0);}

		if (zr!=1)
					return (0);

		int OBJECT_METACLASS = 3;
		if (cla==accelParam[CLASS_METACLASS]){
					if (I_objinclass(obj)||obj==accelParam[CLASS_METACLASS]||obj==accelParam[STRING_METACLASS]||obj==accelParam[ROUTINE_METACLASS]||obj==accelParam[OBJECT_METACLASS])
						return (1);
					return (0);}

		if (cla==accelParam[OBJECT_METACLASS]){
						if (I_objinclass(obj)||obj==accelParam[CLASS_METACLASS]||obj==accelParam[STRING_METACLASS]||obj==accelParam[ROUTINE_METACLASS]||obj==accelParam[OBJECT_METACLASS])
							return (0);
						return (1);}

		if (cla==accelParam[STRING_METACLASS]||cla==accelParam[ROUTINE_METACLASS])
							return (0);

						if (!I_objinclass(cla)){
							fatal("Tried to apply accelerated \"ofclass\" function with non-class.");
							running=false;
							return (0);}

		int inlist=I_rapr(obj,2);

							if (inlist==0)
								return (0);

							int inlistlen=I_rlpr(obj,2)/2;

							for (int jx=0;jx<inlistlen;jx++){
								if (memory.getInt(inlist+jx*4)==cla)
									return (1);}

		return (0);
	}

	private int I_rvpr(int obj,int id)
	{
		int addr=I_rapr(obj,id);

		if (addr==0){
			int CPV_START = 8;
			if (id>0&&id<accelParam[INDIV_PROP_START])
				return (memory.getInt(accelParam[CPV_START]+id*4));
			fatal("Tried to read (something) in accelerated function RV_PR.");
			running=false;
			return (0);}

		return (memory.getInt(addr));
	}

	private int I_oppr(int obj,int id)
	{
		int zr=I_zregion(obj);

		if (zr==3){
			if (id==262||id==263)
				return (1);
			return (0);}

		if (zr==2){
				if (id==261)
					return (1);
				return (0);}

		if (zr!=1)
					return (0);

				if (id>=accelParam[INDIV_PROP_START]&&id<accelParam[INDIV_PROP_START]+8){
					if (I_objinclass(obj))
						return (1);}

		if (I_rapr(obj,id)!=0)
							return (1);

						return (0);
	}

	public boolean getSelecting()
	{
		return (selecting);
	}

	public void setRunning(boolean running)
	{
		this.running=running;
	}
	
	private static void fatal(String s)
	{
		throw new RuntimeException(s);
	}

	private class HuffmanTree
	{
		static final byte NON_LEAF = 0x00;
		static final byte TERM  = 0x01;
		static final byte CHAR = 0x02;
		static final byte C_STRING = 0x03;
		static final byte U_CHAR = 0x04;
		static final byte IND_REF = 0x08;
		static final byte DIND_REF = 0x09;
		static final byte IND_REFA = 0x0a;
		static final byte DIND_REFA = 0xb;

		Node root;
		int startaddr;

		HuffmanTree(int address)
		{
			int len = memory.getInt(address);
			int rootaddr = memory.getInt(address + 8);

			startaddr = address;
			int endaddr = startaddr + len;

			if (rootaddr < endmem && endaddr < ramstart)
				root = readTree(rootaddr, true);
			else
				root = null;
		}

		Node readTree(int rootaddress, boolean recurse)
		{
			Node n = new Node();
			n.type = memory.get(rootaddress);

			switch(n.type)
			{
			case NON_LEAF:
				n.leftaddr = memory.getInt(rootaddress + 1);
				n.rightaddr = memory.getInt(rootaddress + 5);
				if (recurse)
				{
					n.left = readTree(n.leftaddr, true);
					n.right = readTree(n.rightaddr, true);
				}
				break;
			case TERM:
				break;
			case CHAR:
				n.c = memory.get(rootaddress + 1);
				break;
			case C_STRING:
				int len = 0;
				n.addr = rootaddress + 1;
				while (memory.get(n.addr + len) != 0)
					len++;
				n.numargs = len;
				break;
			case U_CHAR:
				n.c=memory.getInt(rootaddress + 1);
				break;
			case IND_REF:
			case DIND_REF:
				n.addr = memory.getInt(rootaddress + 1);
				break;
			case IND_REFA:
			case DIND_REFA:
				n.addr = memory.getInt(rootaddress + 1);
				n.numargs = memory.getInt(rootaddress + 5);
				n.args = new int[n.numargs];
				memory.position(rootaddress + 9);
				for (int i = 0; i < n.numargs; i++)
					n.args[i] = memory.getInt();
				break;
			default:
				fatal("Unknown string decoding tree node type: "+n.type);
			}
			return n;
		}

		class Node
		{
			byte type;
			Node left, right;
			int c;
			int addr;
			int numargs;
			int[] args;
			int leftaddr, rightaddr;
		}
	}
	
	static class AccelStruct
	{
		int funcNum;
		int address;
	}
	
	private int glkCall(int function,int numArgs) throws CloneNotSupportedException, InterruptedException, InvocationTargetException, IOException, ClassNotFoundException
	{
		switch (function){
		case GLKFunctions.GESTALT:
			return (glk.gestalt(funargs[0], funargs[1]));
		case WINDOW_ITERATE:
			return (glk.windowIterate(funargs[0], getGLKRef(1,4), funargs[1]));
		case WINDOW_OPEN:
			return (glk.windowOpen(funargs[0], funargs[1], funargs[2], funargs[3], funargs[4]));
		case WINDOW_GET_SIZE:
			glk.windowGetSize(funargs[0], getGLKRef(1,4), funargs[1], getGLKRef(2,4), funargs[2]);
			return (0);
		case WINDOW_SET_ARRANGEMENT:
			glk.windowSetArrangement(funargs[0], funargs[1], funargs[2], funargs[3]);
			return (0);
		case WINDOW_GET_PARENT:
			return (glk.windowGetParent(funargs[0]));
		case WINDOW_CLEAR:
			glk.windowClear(funargs[0]);
			return (0);
		case WINDOW_MOVE_CURSOR:
			glk.windowMoveCursor(funargs[0], funargs[1], funargs[2]);
			return (0);
		case SET_WINDOW:
			glk.setWindow(funargs[0]);
			return (0);
		case STREAM_ITERATE:
			return (glk.streamIterate(funargs[0], getGLKRef(1,4), funargs[1]));
		case STREAM_OPEN_FILE:
			return (glk.streamOpenFile(funargs[0], funargs[1], funargs[2]));
		case STREAM_OPEN_MEMORY:
			return (glk.streamOpenMemory(getGLKRef(0,funargs[1]), funargs[0], funargs[1], funargs[2], funargs[3]));
		case STREAM_CLOSE:
			glk.streamClose(funargs[0], getGLKRef(1,8), funargs[1]);
			return (0);
		case STREAM_SET_CURRENT:
			glk.streamSetCurrent(funargs[0]);
			return (0);
		case STREAM_GET_CURRENT:
			return (glk.streamGetCurrent());
		case FILEREF_CREATE_BY_NAME:
			return (glk.fileRefCreateByName(funargs[0], getGLKRef(1,0), funargs[1]+1, funargs[2]));
		case FILEREF_CREATE_BY_PROMPT:
			selecting=true;
			int res=(glk.fileRefCreateByPrompt(funargs[0], funargs[1], funargs[2]));
			selecting=false;
			return (res);
		case FILEREF_DESTROY:
			glk.fileRefDestroy(funargs[0]);
			return (0);
		case FILEREF_ITERATE:
			return (glk.fileRefIterate(funargs[0], getGLKRef(1,4), funargs[1]));
		case FILEREF_DELETE_FILE:
			glk.fileRefDeleteFile(funargs[0]);
			return (0);
		case FILEREF_DOES_FILE_EXIST:
			return (glk.fileRefDoesFileExist(funargs[0]));
		case SET_STYLE:
			glk.setStyle(funargs[0]);
			return (0);
		case CHAR_TO_LOWER:
			return (glk.charToLower((byte)funargs[0]));
		case CHAR_TO_UPPER:
			return (glk.charToUpper((byte)funargs[0]));
		case STYLEHINT_SET:
			glk.styleHintSet(funargs[0], funargs[1], funargs[2], funargs[3]);
			return (0);
		case SELECT:
			selecting=true;
			glk.select(getGLKRef(0,16), funargs[0]);
			selecting=false;
			return (0);
		case REQUEST_LINE_EVENT:
			glk.requestLineEvent(funargs[0], getGLKRef(1,funargs[2]), funargs[1], funargs[2], funargs[3]);
			return (0);
		case IMAGE_DRAW:
			return (glk.imageDraw(funargs[0], funargs[1], funargs[2], funargs[3]));
		case SET_HYPERLINK:
			glk.setHyperlink(funargs[0]);
			return (0);
		case REQUEST_HYPERLINK_EVENT:
			glk.requestHyperlinkEvent(funargs[0]);
			return (0);
		case CANCEL_HYPERLINK_EVENT:
			glk.cancelHyperlinkEvent(funargs[0]);
			return (0);
		case PUT_CHAR_UNI:
			glk.putCharUni((byte)funargs[0]);
			return (0);
		case STREAM_OPEN_MEMORY_UNI:
			return (glk.streamOpenMemoryUni(getGLKRef(0,funargs[1]), funargs[0], funargs[1], funargs[2], funargs[3]));
		case STORE_TEXT_BUFFER:
			glk.storeTextBuffer();
			return (0);
		case RESTORE_TEXT_BUFFER:
			glk.restoreTextBuffer();
			return (0);
		case FORM_FEED:
			glk.formFeed();
			return (0);
		case TRIM_PARAGRAPHS:
			return (0);
		case INSERT_TEXT_JUMP:
			glk.insertTextJump(getGLKRef(0,0), funargs[0]+1);
			return (0);
		case SHOW_ALT_IMAGE:
			if (quitPending)
				quit();
			else{
				selecting=true;
				glk.showAltImage(funargs[0]);
				selecting=false;}
			return (0);
		case PRINT_FROM_BOTTOM:
			glk.printFromBottom();
			return (0);
		case TOGGLE_UNDO_MENU:
			glk.toggleUndoMenu(funargs[0]);
			return(0);
		case SET_BACKGROUND_IMAGE:
			glk.setBackgroundImage(funargs[0]);
			return(0);
		default:
			fatal("Unknown GLK function call: "+function);
			return (0);}
	}
	
	private byte[] getGLKRef(int index,int refSize)
	{
		switch (funargs[index]){
		case 0:
			funargs[index]=-1;
			return (null);
		case -1:
			funargs[index]=sp;
			sp+=refSize;
			return (stack.array());
		default:
			return (memory.array());}
	}
	
	public void setQuitPending(boolean quitPending)
	{
		this.quitPending=quitPending;
	}
	
	public void finalize()
	{
		if (memory!=null)
			memory.finalize();
		if (stack!=null)
			stack.finalize();
	}
}
