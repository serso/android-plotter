package org.solovyev.android.plotter.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.solovyev.android.plotter.PlotData;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.PlotIconView;
import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FunctionsDialog {

    @NonNull
    protected final RecyclerView view;

    public FunctionsDialog(@NonNull Context context, @NonNull PlotData plotData) {
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
        b.setNeutralButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                App.getBus().post(new ShowAddFunctionEvent());
            }
        });
        b.show();
    }

    public static final class ShowEvent {
        @NonNull
        public final PlotData plotData;

        public ShowEvent(@NonNull PlotData plotData) {
            this.plotData = plotData;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.function_icon)
        PlotIconView icon;

        @Bind(R.id.function_name)
        TextView name;
        private PlotFunction function;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @NonNull
        public static ViewHolder create(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            return new ViewHolder(inflater.inflate(R.layout.dialog_function, parent, false));
        }

        void bind(@NonNull PlotFunction function) {
            this.function = function;
            name.setText(function.function.getName());
            icon.setMeshSpec(function.meshSpec);
        }

        @Override
        public void onClick(View v) {

        }
    }

    private static class Adapter extends RecyclerView.Adapter {
        @NonNull
        private final List<PlotFunction> list;

        public Adapter(@NonNull List<PlotFunction> list) {
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
