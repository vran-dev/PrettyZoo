package cc.cc1234.antlr4.properties;// Generated from ..\resources\grammars\Properties.g4 by ANTLR 4.9
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PropertiesParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PropertiesVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#propertiesFile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertiesFile(PropertiesParser.PropertiesFileContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(PropertiesParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecl(PropertiesParser.DeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(PropertiesParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(PropertiesParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PropertiesParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(PropertiesParser.CommentContext ctx);
}