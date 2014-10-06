package kfs.kfsAsnDecode;

import java.util.ArrayList;

public class EBlock {

    /**
     * If the EBlock is a dummy EBlock containing only padding bytes, then this attribute is set
     * This happens only if the ASNData has fixed block size. In that case Block.getSubBlocks()
     * returns an array of sub blocks within it, ignoring the isPadded subBlocks within it.
	    *
     */
    public boolean isPadded = false;
    public static int MAX_BLOCKS = -1;

    public int getTag() {
        return tag;
    }

    public int getLength() {
        return length;
    }

    public byte getValueAt(int n) {
        return fileBytes[valStart + n];
    }

    public int getFileOffset() {
        return fileOffset;
    }

    public boolean isLeaf() {
        return isPrimitive;
    }

    /**
     * Total Size of this block
     */
    public int getBlockSize() {
        return valEnd - fileOffset;
    }

    public int getNextBlockOffset() {
        return valEnd;
    }

    public boolean isIndefinateLength() {
        return subBlocks == null ? false : true;
    }

    /**
     * @return true if this Block represents EOC (End of Content)
     */
    boolean isEOC() {
        return ((length == 0) && (tag == 0) && isPrimitive);
    }

    public int getValStart() {
        return valStart;
    }

    public int getNumTagBytes() {
        return numTagBytes;
    }

    public int getNumLenBytes() {
        return numLenBytes;
    }

    public byte getFirstTagByte() {
        return this.firstTagByte;
    }

    public byte[] getFileBytes() {
        return this.fileBytes;
    }
    // =============================================================
    public int tag;
    int length;
    int fileOffset;  	   /*
     * The offset of this block in ASNData File
     */

    // [ Block offset(x) tag(x) length(l) valStart(x) valEnd(x) ]
    /**
     * T L V T
     * Offsets [0] [1] [2] [3]...[100] Value 3 1 7 8 ...
     *
     * Tag(3) Length(1) Value(2) fileOffset = 0 valStart = 2 | fileOffset+numTagBytes+numLenBytes =
     * 0 + 1 + 1 valEnd = 3 | valStart+length | fileOffset+numTagBytes+numLenBytes+length = 0 + 1 +
     * 1 + 1 blockSize = 3 | valEnd - fileOffset 3-1=0 | numTagBytes+numLenBytes+length = 1+1+1=3
     *
     * length = 1 AND valEnd - ValStart 3-2=1
     */
    /*
     * Instead of copying values, it saves memory to just keep track of original bytes in file
     * (fileBytes) as reference with a marker to valueStart and valueEnd
     */
    public byte[] fileBytes;
    public int valStart;  // Offset where the Block value start   | Say = 2
    public int valEnd; // One past the Offset where the Block value ends.  | Say = 3
    // If value is zero bytes then valueStart = valueEnd.
    // If value is 1 bytes then valueStart = offset into fileBytes; and valueEnd = valueStart+1; 
    // In general value length = valueEnd - valueStart;
    /**
     * Tells if this block represents a Primitive Eg. INTEGER, OCTET STRING, etc. Is true if the
     * firstTagByte's bit 5 is set.
     */
    public boolean isPrimitive;
    /**
     * The value of firstTagByte can be used to retrieve class information.
     *
     * Class bit 8 bit 7 ---------------------- Universal 0 0 Application 0 1 Context-specific	1 0
     * Private	1 1
     */
    byte firstTagByte; // Contains ClassBits info. 7th and 8th Bit
    /**
     * Number of bytes used to store the tag.
     */
    private int numTagBytes;
    /**
     * Number of bytes used to store the length.
     */
    private int numLenBytes;
    /**
     * If Block is of indefinite size, then its sub-blocks are stored here. If this is null, it
     * indicates definite length block.
     */
    ArrayList<EBlock> subBlocks;

    // =======================================================================
    public EBlock(int _tag, int _length, byte[] _fileBytes, int _fileOffset, int _valStart, int _valEnd) {
        tag = _tag;
        length = _length;
        fileOffset = _fileOffset;
        fileBytes = _fileBytes;
        valStart = _valStart;
        valEnd = _valEnd;
    }

    /**
     * Detailed Formatted String containing Block Information
     */
    public String toDetailedString(boolean showSubBlocks) {
        String ret = getMetaString() + getValueString(true);

        if (showSubBlocks && isIndefinateLength()) {
            for (int i = 0; i < subBlocks.size(); i++) {
                ret = ret + "\n\t" + subBlocks.get(i).toDetailedString(showSubBlocks);
            }
        }
        return ret;
    }

    public String toConciseString() {
        return getMetaString() + getValueString(false);
    }

    /**
     * @return byte[] even if the Block length is indeterminate.
     */
    public byte[] getValue() {
        int size = (valEnd - valStart);
        byte[] ret = new byte[size];
        for (int i = 0; i < size; i++) {
            ret[i] = fileBytes[ valStart + i];
        }
        return ret;
    }

    String getValueString(boolean complete) {
        byte[] value = getValue();
        String ret;
        if (value != null) {
            if (!complete) {
                ret = " value = <" + toConciseString(value) + "> ]";
            } else {
                ret = " value = <" + toString(value) + "> ]";
            }
        } else {
            ret = "null";
        }
        return ret;
    }

    /**
     * Does not print the Block contents
     */
    public String getMetaString() {
        String ret = "";
        String lenStr = this.isIndefinateLength() ? "" + length + "*" : "" + length;
        String tagStr = this.isEOC() ? "" + tag + "*" : "" + tag;
        tagStr = isPrimitive ? tagStr + "|primitive" : tagStr;

        int intbyte = getFirstTagByte() & (0xff);
        String hexByte = Integer.toString((intbyte & 0xff) + 0x100, 16).substring(1);


        return ret + "BLOCK [ Offset(" + getFileOffset() + ") Tag(" + tagStr + ") Length(" 
                + lenStr + ") valStart(" + valStart + ") "
                + "valEnd(" + this.getNextBlockOffset() + ") "
                + "blockSize(" + getBlockSize() + ") "
                + "firstTagByte(" + hexByte + ") "
                + "numTagbytes(" + this.getNumTagBytes() + ") numLenBytes(" 
                + this.getNumLenBytes() + ")";
    }

    @Override
    public String toString() {
        return toConciseString();
    }

    /*
     * Returns the values of the passed array in a comma separated format
     */
    public static String toString(byte[] arr) {
        String ret = "";
        for (int i = 0; i < arr.length; i++) {

            int intbyte = arr[i] & (0xff);
            String hexByte = Integer.toString((intbyte & 0xff) + 0x100, 16).substring(1);
            ret = ret + hexByte;

            if (i != arr.length - 1) {
                ret = ret + ",";
            }
        }
        return ret;
    }

    /*
     * Returns the values of the passed array in a comma separated format
     */
    public static String toConciseString(byte[] arr) {
        String ret = "";
        boolean dotsPrinted = false;
        for (int i = 0; i < arr.length; i++) {

            if (i == 0 || i == arr.length - 1) { // Only 1st and last element
                int intbyte = arr[i] & (0xff);
                String hexByte = Integer.toString((intbyte & 0xff) + 0x100, 16).substring(1);
                ret = ret + hexByte;

                if (i != arr.length - 1) {
                    ret = ret + ",";
                }
            } else {
                if (!dotsPrinted) {
                    ret = ret + "...,";
                    dotsPrinted = true;
                }
            }
        }
        return ret;
    }

    /**
     * @return true if the tag's 7th and 8th bit are 0	*
     */
    public boolean isUniversal() {
        return (firstTagByte & 0xC0) == 0 ? true : false;
        //  0xC0 = 1100 0000 (7th and 8th Bit are set)
    }

    /**
     * @return true if isUniversal() && !isPrimitive() and (tag==16 || tag==17)
     */
    public boolean isUniversalSetSeq() {
        return ((firstTagByte == 0x30) || (firstTagByte == 0x31)) ? true : false;
        // 0x30 = 0011 0000  (5th Bit and 6th bit are set. 5th says primitive, 6th says SEQ) 
        // 0x31 = 0011 0001  (5th Bit and 6th bit are set. 5th says primitive, 6th says SET)			
    }

    /**
     * Creates a PaddingEBlock object which starts at arr[offset] T = -1 L = Length of the
     * PaddingBlock numTagBytes = numValBytes = 0; valStart - offset valEnd - offset of the next
     * EBlock within arr.
     *
     * @param arr - The byte arr to be used to build the Block
     * @param offset - Offset inside the byte array
     * @param fixedBlockSize - Block size of the data file. Usually 1024 or 4096
     * @param paddingByte - Padding Byte used as filler in the block.
     * @return padded EBlock object
     * @throws ASNException - if arr[offset] does not contain 'paddingByte' or Non paddingByte
     * occurs before end of Fixed Block.
     */
    static EBlock makePaddingEBlock(final byte[] arr, int offset, int fixedBlockSize, byte paddingByte) {
        /*
         * E.g Say offset = 8000 fixedBlockSize = 4096 (means 4096, 8192, 12288, ...)
         */
        int paddingLen = fixedBlockSize - (offset % fixedBlockSize);  // 4096 - 3904 = 192

        for (int i = 0; i < paddingLen; i++) {
            byte b = arr[offset + i];
            if (b != paddingByte) {
                String s = String.format("Non Padding Byte(0x%X) found at offset(%d) Expected Padding Byte(0x%X)", b, offset + i, paddingByte);
                throw new ASNException(s);
            }
        }
        //public EBlock( int _tag, int _length, byte[] _fileBytes,int _fileOffset,  int _valStart, int _valEnd  ) {
        EBlock retBlock = new EBlock(-1, paddingLen, arr, offset, offset, offset + paddingLen);
        retBlock.isPadded = true;
        //System.out.printf("\n makePaddingBlock( %s ) ", retBlock.toConciseString() );
        return retBlock;
    }

    /**
     *
     * @param arr - byte array to be used to build the Block
     * @param offset - offset inside the byte array
     * @param parentValueOffset - The offset of arr in file. parentValueOffset + offset should give
     * the offset of returned block.
     *
     * @return Block found at offset in arr, else returns null.
     */
    public static EBlock getBlock(final byte[] arr, int offset, int fixedBlockSize, byte paddingByte) { //, int parentValueOffset) {
        //System.out.printf("\n  getBlock(arr.length=%d) offset=%d | %d ",arr.length,offset, offset+262);
        if (offset >= arr.length) {
            return null; // Defensive check 
        }
        int pos = offset;

        EBlock retBlock = new EBlock(0, 0, arr, offset, 0, 0); // will return this.
        //retBlock.fileOffset = offset;  // Set in the constructor.
        retBlock.numLenBytes = 1;
        retBlock.numTagBytes = 1;

        byte tag = arr[pos++];
        retBlock.firstTagByte = tag;
        retBlock.isPrimitive = ((tag & (1L << 5)) == 0) ? true : false;     // If 5th bit is not set
        boolean isMultiByteTag = ((tag & 0x1F) == 0x1F) ? true : false; // least significant 5 bits are set?

        if (fixedBlockSize != 0) { // Do I Need to check if this is a Padded EBlock
            if (tag == paddingByte) {
                return makePaddingEBlock(arr, offset, fixedBlockSize, paddingByte);
            }
        }
        if (isMultiByteTag) { //offset 350 in eric.dat has 3 bytes (not handled)
            boolean moreTagBytesExist = true;

            while (moreTagBytesExist) {
                byte a = arr[pos++];
                retBlock.numTagBytes++;
                int currentByte = a & 0xFF;
                moreTagBytesExist = ((a & (1L << 7)) == 0) ? false : true;     // If MSB bit is set

                //System.out.printf("\n ## currentByte(%2x) %s %s, numBytesTag(%d) ",currentByte, ASNTest.getBitStrByte(a), ASNTest.getBitStrInt(currentByte), numBytesTag );
                retBlock.tag = (retBlock.tag << 7) + (currentByte & ~(1 << 7));
                if (retBlock.numTagBytes > 8) {
                    throw new ASNException("Tag too long. Check if ASNData is Valid at offset " + offset + " arr.length = " + arr.length);
                }
            }
        } else {
            retBlock.tag = (tag & 0xFF) & 0x1F; // make 5th,6th,7th bit 0
        }
        boolean isMultiByteLength = ((arr[pos] & 0xFF) & (1L << 7)) != 0; //Is multibyte length ?

        if (isMultiByteLength) {
            //System.out.printf("\n here... is multiByteLength");
            int extraNumBytesLength = ((arr[pos++] & 0xFF) & 0x7F); // remaining 7 bits tell the num of bytes used for length
            if (extraNumBytesLength == 0) {// Indefinite Length
                //System.out.printf("\n here.. IsIndefinite Length");
                retBlock.subBlocks = new ArrayList<EBlock>();
                int bytesReadSoFar = retBlock.numTagBytes + retBlock.numLenBytes;
                EBlock innerBlock = getBlock(arr, offset + bytesReadSoFar, 0, (byte) 0); // fixedBlockSize is not recursively used. 
                while (!innerBlock.isEOC()) {
                    retBlock.subBlocks.add(innerBlock);
                    innerBlock = getBlock(arr, innerBlock.valEnd, 0, (byte) 0); // fixedBlockSize is not recursively used.
                }
                retBlock.valStart = offset + retBlock.numTagBytes + retBlock.numLenBytes;
                retBlock.valEnd = innerBlock.valEnd;
                retBlock.length = retBlock.valEnd - retBlock.valStart;
                //retBlock.blockSize = retBlock.numTagBytes + retBlock.numLenBytes + retBlock.length;
                //offset + retBlock.numTagBytes + retBlock.numLenBytes + retBlock.length;
                ////System.out.printf(" return block.length %s blocksize %s blockstart %s", retBlock.length, retBlock.blockSize , retBlock.blockStart	);

                return retBlock;
            }
            for (int i = 0; i < extraNumBytesLength; i++) {
                int nextLengthByte = arr[pos++] & 0xFF;
                retBlock.length = (retBlock.length << 8) + nextLengthByte;
            }
            retBlock.numLenBytes = 1 + extraNumBytesLength;
        } else {
            //System.out.printf("\n here... is singleByteLength");
            retBlock.length = arr[pos++] & 0xFF;
            // Check for EOC Block
            if (retBlock.length == 0 && retBlock.isPrimitive && (retBlock.tag == 0)) { // is EOC 

                retBlock.valStart = offset + retBlock.numTagBytes + retBlock.numLenBytes;
                retBlock.valEnd = retBlock.valStart;
                //retBlock.blockSize = retBlock.numTagBytes + retBlock.numLenBytes;
                //retBlock.blockStart = offset + retBlock.blockSize;

                ////System.out.printf(" return blocksize %s blockstart %s", retBlock.blockSize , retBlock.blockStart	);
                return retBlock;
            }
        }
        retBlock.valStart = offset + retBlock.numTagBytes + retBlock.numLenBytes;
        retBlock.valEnd = retBlock.valStart + retBlock.length;

        /*
         * retBlock.value = new byte[retBlock.length]; myArrayCopy( arr , pos, retBlock.value,0,
         * retBlock.length );
         *
         * //System.out.printf("\n-- %d %d %d -- ", numBytesTag , numBytesLength, retBlock.length);
         * retBlock.blockSize = retBlock.numTagBytes + retBlock.numLenBytes + retBlock.length;
         * retBlock.blockStart = offset + retBlock.blockSize;
         */
        ////System.out.printf(" return block.length %s blocksize %s blockstart %s | %s ", retBlock.length, retBlock.blockSize , retBlock.blockStart, Block.toString(retBlock.value) );

        return retBlock;
    }

    /*
     * private static void myArrayCopy(byte[] src,int src_offset,byte[] dest,int dest_offset,int
     * numBytes) { if( src == null || dest == null ) { throw new ASNException("src || dest is
     * null"); }
     *
     * if( (src_offset + numBytes) > src.length ) { String err = String.format("\n(src_offset(%d) +
     * numBytes(%d))= %d > src.length(%d)", src_offset, numBytes,(src_offset+numBytes), src.length
     * ); throw new ASNException( err ); }
     *
     * if( (dest_offset+ numBytes) > dest.length ) { throw new ASNException("(dest_offset+ numBytes)
     * > dest.length"); }
     *
     * System.arraycopy(src,src_offset,dest,dest_offset, numBytes );
		}
     */
    // _debugOffset here means the debugOffset of the parent Block  which is calling this method by passing parent.value,maxSubBlocks,parent.debugOffset
    /**
     * Creates an ArrayList<Block> from byte[] If 'maxSubBlocks' == MAX_BLOCKS, creates as many
     * Blocks as possible else creates max of 'maxSubBlocks'
     *
     * @param start - like valueStart - Index of starting byte[] element
     * @param end - like valueEnd - Index of one past last element of byte[]
     *
     */
    private static ArrayList<EBlock> getSubBlocks_private(final byte[] byteArr, int start, int end, int maxSubBlocks, int fixedBlockSize, byte paddingByte) {
        //System.out.printf("\n start getBlocks_private(arr.length=%d) ",byteArr.length);

        ArrayList<EBlock> ret = new ArrayList<EBlock>();
        EBlock b = new EBlock(0, 0, byteArr, 0, 0, start);  // b.valEnd = start
        int i = 0;
        while (b.valEnd < end && (b = EBlock.getBlock(byteArr, b.valEnd, fixedBlockSize, paddingByte)) != null) {
            // start of long code for debugging ---------------
	    	   /*
             * while ( true ) { // b.valEnd < end && (b = EBlock.getBlock(byteArr,b.valEnd,
             * fixedBlockSize, paddingByte )) != null boolean breakout = true ; if( b.valEnd < end )
             * { System.out.printf("\n b.valEnd = %d",b.valEnd ); b =
             * EBlock.getBlock(byteArr,b.valEnd, fixedBlockSize, paddingByte ); if ( b != null ) {
             * breakout = false;	} else { System.out.printf("\n Hit Null Block " ); } } else {
             * System.out.printf("\n b.valEnd = %d | should break out now.",b.valEnd );	} if(
             * breakout ) break;
             */
            // end of long code for debugging -----------------


            if (maxSubBlocks != MAX_BLOCKS && i >= maxSubBlocks) {
                break;
            }
            if (!b.isPadded) {
                ret.add(b);
            }
            //System.out.printf("\n getSubBlockprivate( b.valEnd(%d) end(%d) b(%s) )", b.valEnd, end, b.toConciseString() );
            i++;
        }
        //if( i > 20000 ) { System.out.println("Something is Fishy"); }
        //System.out.printf("\n end getBlocks_private(arr.length=%d) ",byteArr.length);
        return ret;
    }

    /**
     *
     * @param maxSubBlocks if -1 returns max possible subBlocks maxSubBlocks is ignored if this
     * Block is of indefinite length
     * @return sub blocks within this block.
     */
    public ArrayList<EBlock> getSubBlocks(int maxSubBlocks, int fixedBlockSize, byte paddingByte) {
        //System.out.printf("\nstart getSubBlocks(isIndefinateLength=%s)",isIndefinateLength());
        ArrayList<EBlock> listBlk = null;
        if (isIndefinateLength()) {
            listBlk = subBlocks;
        } else {
            listBlk = EBlock.getSubBlocks_private(this.fileBytes, this.valStart, this.valEnd, maxSubBlocks, fixedBlockSize, paddingByte);
        }
        return listBlk;
        /*
         * EBlock[] arrBlk = new EBlock[ listBlk.size() ]; listBlk.toArray(arrBlk);
         * //System.out.printf("\nend getSubBlocks(isIndefinateLength=%s)",isIndefinateLength());
         * return arrBlk;
         */
    }

    /**
     * Scrambles all data in EBlock with garbage values!! Keeps the structural integrity of the
     * EBlock, i.e only leaf EBlock values are garbled.
     */
    public void scramble() {
        if (!isPrimitive) {
            return;  // we are interested only in leaf nodes
        }
        // clear the data segment.
        for (int i = valStart; i < valEnd; i++) {
            fileBytes[i] = 0;
        }
    }
}
