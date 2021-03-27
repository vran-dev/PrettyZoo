package cc.cc1234.antlr4.json;// Generated from ..\resources\grammars\JSON.g4 by ANTLR 4.9

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSONLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		STRING=10, NUMBER=11, WS=12;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"STRING", "ESC", "UNICODE", "HEX", "SAFECODEPOINT", "NUMBER", "INT", 
			"EXP", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "','", "'}'", "':'", "'['", "']'", "'true'", "'false'", 
			"'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "STRING", 
			"NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JSONLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JSON.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\16\u0082\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3"+
		"\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3"+
		"\13\7\13G\n\13\f\13\16\13J\13\13\3\13\3\13\3\f\3\f\3\f\5\fQ\n\f\3\r\3"+
		"\r\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\5\20^\n\20\3\20\3\20\3\20"+
		"\6\20c\n\20\r\20\16\20d\5\20g\n\20\3\20\5\20j\n\20\3\21\3\21\3\21\7\21"+
		"o\n\21\f\21\16\21r\13\21\5\21t\n\21\3\22\3\22\5\22x\n\22\3\22\3\22\3\23"+
		"\6\23}\n\23\r\23\16\23~\3\23\3\23\2\2\24\3\3\5\4\7\5\t\6\13\7\r\b\17\t"+
		"\21\n\23\13\25\f\27\2\31\2\33\2\35\2\37\r!\2#\2%\16\3\2\n\n\2$$\61\61"+
		"^^ddhhppttvv\5\2\62;CHch\5\2\2!$$^^\3\2\62;\3\2\63;\4\2GGgg\4\2--//\5"+
		"\2\13\f\17\17\"\"\2\u0086\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2"+
		"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2"+
		"\25\3\2\2\2\2\37\3\2\2\2\2%\3\2\2\2\3\'\3\2\2\2\5)\3\2\2\2\7+\3\2\2\2"+
		"\t-\3\2\2\2\13/\3\2\2\2\r\61\3\2\2\2\17\63\3\2\2\2\218\3\2\2\2\23>\3\2"+
		"\2\2\25C\3\2\2\2\27M\3\2\2\2\31R\3\2\2\2\33X\3\2\2\2\35Z\3\2\2\2\37]\3"+
		"\2\2\2!s\3\2\2\2#u\3\2\2\2%|\3\2\2\2\'(\7}\2\2(\4\3\2\2\2)*\7.\2\2*\6"+
		"\3\2\2\2+,\7\177\2\2,\b\3\2\2\2-.\7<\2\2.\n\3\2\2\2/\60\7]\2\2\60\f\3"+
		"\2\2\2\61\62\7_\2\2\62\16\3\2\2\2\63\64\7v\2\2\64\65\7t\2\2\65\66\7w\2"+
		"\2\66\67\7g\2\2\67\20\3\2\2\289\7h\2\29:\7c\2\2:;\7n\2\2;<\7u\2\2<=\7"+
		"g\2\2=\22\3\2\2\2>?\7p\2\2?@\7w\2\2@A\7n\2\2AB\7n\2\2B\24\3\2\2\2CH\7"+
		"$\2\2DG\5\27\f\2EG\5\35\17\2FD\3\2\2\2FE\3\2\2\2GJ\3\2\2\2HF\3\2\2\2H"+
		"I\3\2\2\2IK\3\2\2\2JH\3\2\2\2KL\7$\2\2L\26\3\2\2\2MP\7^\2\2NQ\t\2\2\2"+
		"OQ\5\31\r\2PN\3\2\2\2PO\3\2\2\2Q\30\3\2\2\2RS\7w\2\2ST\5\33\16\2TU\5\33"+
		"\16\2UV\5\33\16\2VW\5\33\16\2W\32\3\2\2\2XY\t\3\2\2Y\34\3\2\2\2Z[\n\4"+
		"\2\2[\36\3\2\2\2\\^\7/\2\2]\\\3\2\2\2]^\3\2\2\2^_\3\2\2\2_f\5!\21\2`b"+
		"\7\60\2\2ac\t\5\2\2ba\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2eg\3\2\2\2"+
		"f`\3\2\2\2fg\3\2\2\2gi\3\2\2\2hj\5#\22\2ih\3\2\2\2ij\3\2\2\2j \3\2\2\2"+
		"kt\7\62\2\2lp\t\6\2\2mo\t\5\2\2nm\3\2\2\2or\3\2\2\2pn\3\2\2\2pq\3\2\2"+
		"\2qt\3\2\2\2rp\3\2\2\2sk\3\2\2\2sl\3\2\2\2t\"\3\2\2\2uw\t\7\2\2vx\t\b"+
		"\2\2wv\3\2\2\2wx\3\2\2\2xy\3\2\2\2yz\5!\21\2z$\3\2\2\2{}\t\t\2\2|{\3\2"+
		"\2\2}~\3\2\2\2~|\3\2\2\2~\177\3\2\2\2\177\u0080\3\2\2\2\u0080\u0081\b"+
		"\23\2\2\u0081&\3\2\2\2\16\2FHP]dfipsw~\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}