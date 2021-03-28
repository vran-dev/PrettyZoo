package cc.cc1234.antlr4.properties;// Generated from ..\resources\grammars\Properties.g4 by ANTLR 4.9
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PropertiesParser}.
 */
public interface PropertiesListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#propertiesFile}.
	 * @param ctx the parse tree
	 */
	void enterPropertiesFile(PropertiesParser.PropertiesFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#propertiesFile}.
	 * @param ctx the parse tree
	 */
	void exitPropertiesFile(PropertiesParser.PropertiesFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#row}.
	 * @param ctx the parse tree
	 */
	void enterRow(PropertiesParser.RowContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#row}.
	 * @param ctx the parse tree
	 */
	void exitRow(PropertiesParser.RowContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#decl}.
	 * @param ctx the parse tree
	 */
	void enterDecl(PropertiesParser.DeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#decl}.
	 * @param ctx the parse tree
	 */
	void exitDecl(PropertiesParser.DeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(PropertiesParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(PropertiesParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(PropertiesParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(PropertiesParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link PropertiesParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(PropertiesParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link PropertiesParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(PropertiesParser.CommentContext ctx);
}