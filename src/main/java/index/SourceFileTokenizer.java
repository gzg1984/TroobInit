package index;

import java.io.IOException;

import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.CharacterUtils.CharacterBuffer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class SourceFileTokenizer extends Tokenizer {
	private int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;
	private static final int IO_BUFFER_SIZE = 4096;
	private final CharTermAttribute termAtt = addAttribute(
			CharTermAttribute.class);
	private final CharacterBuffer ioBuffer = CharacterUtils
			.newCharacterBuffer(IO_BUFFER_SIZE);
	private final OffsetAttribute offsetAtt = addAttribute(
			OffsetAttribute.class);
	
	private final int maxTokenLen;
	private int lastChar;
	public static final int DEFAULT_MAX_WORD_LEN = 255;
	private boolean hasMultilineComment=false;
	private boolean hasSinglelineComment=false;
	private boolean hasString=false;
	private int skippedPositions;
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	public SourceFileTokenizer() {
		this.maxTokenLen = DEFAULT_MAX_WORD_LEN;
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		skippedPositions = 0;
		int length = 0;
		int start = -1; // this variable is always initialized
		int end = -1;
		char[] buffer = termAtt.buffer();
		while (true) {
			if (bufferIndex >= dataLen) {
				offset += dataLen;
				CharacterUtils.fill(ioBuffer, input); // read supplementary char
//														// aware with
//														// CharacterUtils
				if (ioBuffer.getLength() == 0) {
					dataLen = 0; // so next offset += dataLen won't decrement
									// offset
					if (length > 0) {
						break;
					} else {
						finalOffset = correctOffset(offset);
						return false;
					}
				}
				dataLen = ioBuffer.getLength();
				bufferIndex = 0;
			}
			// use CharacterUtils here to support < 3.1 UTF-16 code unit
			// behavior if the char based methods are gone
			final int c = Character.codePointAt(ioBuffer.getBuffer(),
					bufferIndex, ioBuffer.getLength());
			final int charCount = Character.charCount(c);
			bufferIndex += charCount;
//			System.out.println((char)c);
			if((c==42&&lastChar==47)){
				hasMultilineComment=true;
				assert length>0;
				
				// start of multi-comments
//				if(length>= buffer.length - 1){
//					buffer = termAtt.resizeBuffer(2 + length);
//				}
//				end += charCount;
//				length += Character.toChars(c, buffer, length);
			}else if(c==47&&lastChar==47&&!hasMultilineComment){
				hasSinglelineComment=true;
			}

			if(!hasSinglelineComment&&!hasMultilineComment&&c==34&&lastChar!=92&&!hasString){ // "" double " " string
				hasString=true;
				lastChar=c;
				skippedPositions++;
				continue;
			}
			if((c==47&&lastChar==42)&&!hasSinglelineComment){
				hasMultilineComment=false;
				lastChar=c;
				start = offset + bufferIndex - charCount;
				end = start;
				length=0;
				skippedPositions++;
				continue;
//				break;
			}
			if((c==10||c==13)&&hasSinglelineComment){
				hasSinglelineComment=false;
				lastChar=c;
				start = offset + bufferIndex - charCount;
				end = start;
				length=0;
				skippedPositions++;
				continue;
			}
			if(hasString&&c==34&&lastChar!=92){
				hasString=false;
				lastChar=c;
				skippedPositions++;
				continue;
			}
			if (isTokenChar(c)&&!hasMultilineComment&&!hasSinglelineComment&&!hasString) {
				if (length == 0) { // start of token
					assert start == -1;
					start = offset + bufferIndex - charCount;
					end = start;
				} else if (length >= buffer.length - 1) {
					buffer = termAtt.resizeBuffer(2 + length); 
				}
				end += charCount;
				length += Character.toChars(c, buffer, length);
			} else if (length > 0&&!hasMultilineComment&&!hasSinglelineComment&&!hasString) { // at non-Letter w/ chars
				break; // return 'em
			}
			lastChar=c;
//			if (isTokenChar(c)&&!hasMultilineComment) { // if it's a token char
//				if (length == 0) { // start of token
//					assert start == -1;
//					start = offset + bufferIndex - charCount;
//					end = start;
//				} else if (length >= buffer.length - 1) { // check if a
//															// supplementary
//															// could run out of
//															// bounds
//					buffer = termAtt.resizeBuffer(2 + length); // make sure a
//																// supplementary
//																// fits in the
//																// buffer
//				}
////				int lastChar;
////				if(bufferIndex>0){
////					lastChar=buffer[bufferIndex-1];
////					if((c==42&&lastChar==47)||(c==47&&lastChar==47)){
////						
////					}
////				}
//				
//				end += charCount;
//				length += Character.toChars(c, buffer, length); // buffer
//																// it,
//																// normalized
//				if (length >= maxTokenLen) { // buffer overflow! make sure to
//												// check for >= surrogate pair
//												// could break == test
//					break;
//				}
//			} else if (length > 0&&!hasMultilineComment) { // at non-Letter w/ chars
//				break; // return 'em
//			}
			
		}

		termAtt.setLength(length);
		assert start != -1;
		offsetAtt.setOffset(correctOffset(start),
				finalOffset = correctOffset(end));
		posIncrAtt.setPositionIncrement(skippedPositions+1);
		return true;
	}

	@Override
	public final void end() throws IOException {
		super.end();
		// set final offset
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = 0;
		offset = 0;
		dataLen = 0;
		finalOffset = 0;
		ioBuffer.reset(); // make sure to reset the IO buffer!!
	}

	private boolean isTokenChar(int c) {
		if(Character.isWhitespace(c)){
			return false;
		}else if(c==46){	// .
			return false;
		}else if(c==123){  	// {
			return false;
		}else if(c==125){ 	// }
			return false; 
		}else if(c==58){	// :
			return false;
		}else if(c==59){	// ;
			return false;
		}else if(c==60){	// <
			return false;
		}else if(c==61){	// =
			return false;
		}else if(c==62){	// >
			return false; 
		}else if(c==63){	// ?
			return false; 
		}else if(c==37){	// %
			return false; 
		}else if(c==40){	// (
			return false; 
		}else if(c==41){	// )
			return false; 
		}else if(c==44){	// ,
			return false; 
		}else if(c==42){	// *
			return false; 
		}else if(c==43){ 	// +
			return false;
		}else if(c==45){	// -
			return false; 
		}else if(c==91){	// [
			return false; 
		}else if(c==93){	// ]
			return false; 
		}

		return true;
	}
}
