package edu.uncc.sis.aside.visitors;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import edu.uncc.sis.aside.AsidePlugin;

public class FindNodeVisitor extends ASTVisitor {

	private ASTNode base, target, match;

	private ASTMatcher matcher;

	public FindNodeVisitor(ASTNode base, ASTNode target) {
		this.base = base;
		this.target = target;
		matcher = AsidePlugin.getDefault().getASTMatcher();
	}

	@Override
	public void postVisit(ASTNode node) {
		super.postVisit(node);
	}

	@Override
	public boolean preVisit2(ASTNode node) {

		boolean matched = target.subtreeMatch(matcher, node);

		if (matched) {
			match = node;
			return false;
		}

		return true;
	}

	public void process() {
		base.accept(this);
	}

	public ASTNode getNode() {
		return match;
	}

}
