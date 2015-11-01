package org.solovyev.android.plotter.app;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import org.solovyev.android.plotter.PlotData;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.PlotIconView;
import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import javax.annotation.Nonnull;
import java.util.List;

public class FunctionsDialog {

	@Nonnull
	protected final RecyclerView view;

	public FunctionsDialog(@Nonnull Context context, @Nonnull PlotData plotData) {
		view = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.dialog_functions, null);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
		layoutManager.setChildSize(context.getResources().getDimensionPixelSize(R.dimen.list_item_height));
		view.setLayoutManager(layoutManager);
		view.addItemDecoration(new DividerItemDecoration(context, null));
		view.setAdapter(new Adapter(plotData.functions));
	}

	public void show() {
		final AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
		b.setView(view);
		b.setCancelable(true);
		b.setPositiveButton(android.R.string.ok, null);
		b.show();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		@Bind(R.id.function_icon)
		PlotIconView icon;

		@Bind(R.id.function_name)
		TextView name;
		private PlotFunction function;

		private ViewHolder(@Nonnull View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			itemView.setOnClickListener(this);
		}

		void bind(@Nonnull PlotFunction function) {
			this.function = function;
			name.setText(function.function.getName());
			icon.setMeshSpec(function.meshSpec);
		}

		@Nonnull
		public static ViewHolder create(@Nonnull LayoutInflater inflater, @Nonnull ViewGroup parent) {
			return new ViewHolder(inflater.inflate(R.layout.dialog_function, parent, false));
		}

		@Override
		public void onClick(View v) {

		}
	}

	private static class Adapter extends RecyclerView.Adapter {
		@Nonnull
		private final List<PlotFunction> list;

		public Adapter(@Nonnull List<PlotFunction> list) {
			this.list = list;
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return ViewHolder.create(LayoutInflater.from(parent.getContext()), parent);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			((ViewHolder) holder).bind(list.get(position));
		}

		@Override
		public int getItemCount() {
			return list.size();
		}
	}
}
