package org.solovyev.android.plotter.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.solovyev.android.plotter.BasePlotterListener;
import org.solovyev.android.plotter.PlotFunction;
import org.solovyev.android.plotter.PlotIconView;
import org.solovyev.android.plotter.Plotter;
import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static android.view.Menu.NONE;

public class FunctionsDialog extends BaseDialogFragment {

    @NonNull
    private final Plotter plotter = App.getPlotter();
    @NonNull
    private final PlotterListener plotterListener = new PlotterListener();
    private Adapter adapter;

    public FunctionsDialog() {
    }

    @NonNull
    public static FunctionsDialog create() {
        return new FunctionsDialog();
    }

    @Subscribe
    public void onDeleteFunction(@NonNull DeleteFunctionEvent e) {
        plotter.remove(e.function);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        App.getBus().register(this);
        plotter.addListener(plotterListener);
        return view;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                final Button neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        App.getBus().post(new AddFunctionDialog.ShowEvent());
                    }
                });
            }
        });
        return dialog;
    }

    @NonNull
    protected RecyclerView onCreateDialogView(@NonNull Context context, @NonNull LayoutInflater inflater, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final RecyclerView view = (RecyclerView) inflater.inflate(R.layout.dialog_functions, null);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context, VERTICAL, false);
        final int itemHeight = context.getResources().getDimensionPixelSize(R.dimen.list_item_height);
        layoutManager.setChildSize(itemHeight + getDividerHeight(context));
        view.setLayoutManager(layoutManager);

        view.addItemDecoration(new DividerItemDecoration(context, null));
        adapter = new Adapter(plotter.getPlotData().functions);
        view.setAdapter(adapter);
        return view;
    }

    private int getDividerHeight(@NonNull Context context) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.listDivider});
        final Drawable divider = a.getDrawable(0);
        final int dividerHeight = divider == null ? 0 : divider.getIntrinsicHeight();
        a.recycle();
        return dividerHeight;
    }

    @Override
    public void onDestroyView() {
        plotter.removeListener(plotterListener);
        App.getBus().unregister(this);
        super.onDestroyView();
    }

    protected void onPrepareDialog(@NonNull AlertDialog.Builder builder) {
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNeutralButton("Add", null);
    }

    public static final class ShowEvent {
        public ShowEvent() {
        }
    }

    public static final class DeleteFunctionEvent {
        @NonNull
        public final PlotFunction function;

        public DeleteFunctionEvent(@NonNull PlotFunction function) {
            this.function = function;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        @Bind(R.id.function_icon)
        PlotIconView icon;

        @Bind(R.id.fn_name_edittext)
        TextView name;
        private PlotFunction function;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @NonNull
        public static ViewHolder create(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            return new ViewHolder(inflater.inflate(R.layout.dialog_functions_function, parent, false));
        }

        void bind(@NonNull PlotFunction function) {
            this.function = function;
            name.setText(function.function.getName());
            icon.setMeshSpec(function.meshSpec);
        }

        @Override
        public void onClick(View v) {
            if (function == null) {
                return;
            }
            final EditFunctionDialog.ShowEvent event = EditFunctionDialog.ShowEvent.tryCreate(function);
            if (event == null) {
                return;
            }
            App.getBus().post(event);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(NONE, R.string.fn_delete, NONE, R.string.fn_delete).setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (function != null && item.getItemId() == R.string.fn_delete) {
                App.getBus().post(new DeleteFunctionEvent(function));
                return true;
            }
            return false;
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

        public void remove(@NonNull PlotFunction function) {
            final int i = list.indexOf(function);
            if (i >= 0) {
                list.remove(i);
                notifyItemRemoved(i);
            }
        }

        public void update(int id, @NonNull PlotFunction function) {
            final int i = find(id);
            if (i >= 0) {
                list.set(i, function);
                notifyItemChanged(i);
            }
        }

        private int find(int id) {
            for (int i = 0; i < list.size(); i++) {
                final PlotFunction function = list.get(i);
                if (function.function.getId() == id) {
                    return i;
                }
            }
            return -1;
        }

        public void add(@NonNull PlotFunction function) {
            list.add(function);
            notifyItemInserted(list.size() - 1);
        }
    }

    private class PlotterListener extends BasePlotterListener {
        @Override
        public void onFunctionAdded(@NonNull PlotFunction function) {
            adapter.add(function);
        }

        @Override
        public void onFunctionUpdated(int id, @NonNull PlotFunction function) {
            adapter.update(id, function);
        }

        @Override
        public void onFunctionRemoved(@NonNull PlotFunction function) {
            adapter.remove(function);
        }
    }
}
