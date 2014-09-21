package org.solovyev.android.plotter;

interface SuperFunction {

	int getArity();

	float evaluate();

	float evaluate(float x);

	float evaluate(float x, float y);
}
