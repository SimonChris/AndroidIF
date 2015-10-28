package sichris.androidDemo.vm;

class OP implements OPConstants
{
	  static final OP[] OPS = new OP[0x182];

	  int arity;
	  int[] format;

	  OP(int a, int[] f)
	  {
	    arity = a;
	    format = f;
	  }

	  static
	  {
	    OPS[NOP] = new OP(0, null);
	    OPS[ADD] = new OP(3, LLS);
	    OPS[SUB] = new OP(3, LLS);
	    OPS[MUL] = new OP(3, LLS);
	    OPS[DIV] = new OP(3, LLS);
	    OPS[MOD] = new OP(3, LLS);
	    OPS[NEG] = new OP(2, LS);
	    OPS[BITAND] = new OP(3, LLS);
	    OPS[BITOR] = new OP(3, LLS);
	    OPS[BITXOR] = new OP(3, LLS);
	    OPS[BITNOT] = new OP(2, LS);
	    OPS[SHIFTL] = new OP(3, LLS);
	    OPS[SSHIFTR] = new OP(3, LLS);
	    OPS[USHIFTR] = new OP(3, LLS);
	    OPS[JUMP] = new OP(1, L);
	    OPS[JZ] = new OP(2, LL);
	    OPS[JNZ] = new OP(2, LL);
	    OPS[JEQ] = new OP(3, LLL);
	    OPS[JNE] = new OP(3, LLL);
	    OPS[JLT] = new OP(3, LLL);
	    OPS[JGE] = new OP(3, LLL);
	    OPS[JGT] = new OP(3, LLL);
	    OPS[JLE] = new OP(3, LLL);
	    OPS[JLTU] = new OP(3, LLL);
	    OPS[JGEU] = new OP(3, LLL);
	    OPS[JGTU] = new OP(3, LLL);
	    OPS[JLEU] = new OP(3, LLL);
	    OPS[CALL] = new OP(3, LLS);
	    OPS[RETURN] = new OP(1, L);
	    OPS[CATCH] = new OP(2, SL);
	    OPS[THROW] = new OP(2, LL);
	    OPS[TAILCALL] = new OP(2, LL);
	    OPS[COPY] = new OP(2, LS);
	    OPS[COPYS] = new OP(2, LS);
	    OPS[COPYB] = new OP(2, LS);
	    OPS[SEXS] = new OP(2, LS);
	    OPS[SEXB] = new OP(2, LS);
	    OPS[ALOAD] = new OP(3, LLS);
	    OPS[ALOADS] = new OP(3, LLS);
	    OPS[ALOADB] = new OP(3, LLS);
	    OPS[ALOADBIT] = new OP(3, LLS);
	    OPS[ASTORE] = new OP(3, LLL);
	    OPS[ASTORES] = new OP(3, LLL);
	    OPS[ASTOREB] = new OP(3, LLL);
	    OPS[ASTOREBIT] = new OP(3, LLL);
	    OPS[STKCOUNT] = new OP(1, S);
	    OPS[STKPEEK] = new OP(2, LS);
	    OPS[STKSWAP] = new OP(0, null);
	    OPS[STKROLL] = new OP(2, LL);
	    OPS[STKCOPY] = new OP(1, L);
	    OPS[STREAMCHAR] = new OP(1, L);
	    OPS[STREAMNUM] = new OP(1, L);
	    OPS[STREAMSTR] = new OP(1, L);
	    OPS[STREAMUNICHAR] = new OP(1, L);
	    OPS[GESTALT] = new OP(3, LLS);
	    OPS[DEBUGTRAP] = new OP(1, L);
	    OPS[GETMEMSIZE] = new OP(1, S);
	    OPS[SETMEMSIZE] = new OP(2, LS);
	    OPS[JUMPABS] = new OP(1, L);
	    OPS[RANDOM] = new OP(2, LS);
	    OPS[SETRANDOM] = new OP(1, L);
	    OPS[QUIT] = new OP(0, null);
	    OPS[VERIFY] = new OP(1, S);
	    OPS[RESTART] = new OP(0, null);
	    OPS[SAVE] = new OP(2, LS);
	    OPS[RESTORE] = new OP(2, LS);
	    OPS[SAVEUNDO] = new OP(1, S);
	    OPS[RESTOREUNDO] = new OP(1, S);
	    OPS[PROTECT] = new OP(2, LL);
	    OPS[GLK] = new OP(3, LLS);
	    OPS[GETSTRINGTBL] = new OP(1, S);
	    OPS[SETSTRINGTBL] = new OP(1, L);
	    OPS[GETIOSYS] = new OP(2, SS);
	    OPS[SETIOSYS] = new OP(2, LL);
	    OPS[LINEARSEARCH] = new OP(8, LLLLLLLS);
	    OPS[BINARYSEARCH] = new OP(8, LLLLLLLS);
	    OPS[LINKEDSEARCH] = new OP(7, LLLLLLS);
	    OPS[CALLF] = new OP(2, LS);
	    OPS[CALLFI] = new OP(3, LLS);
	    OPS[CALLFII] = new OP(4, LLLS);
	    OPS[CALLFIII] = new OP(5, LLLLS);
	    OPS[ACCELFUNC] = new OP(2, LL);
	    OPS[ACCELPARAM] = new OP(2, LL);
	  }
}
