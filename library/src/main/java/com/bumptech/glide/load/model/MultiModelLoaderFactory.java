package com.bumptech.glide.load.model;

import android.content.Context;

import com.bumptech.glide.util.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiModelLoaderFactory {
    private final List<Entry<?, ?>> entries = new ArrayList<Entry<?, ?>>();
    private final Context context;

    public MultiModelLoaderFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    <Model, Data> void append(Class<Model> modelClass, Class<Data> dataClass, ModelLoaderFactory<Model, Data> factory) {
        add(modelClass, dataClass, factory, true /*append*/);
    }

    <Model, Data> void prepend(Class<Model> modelClass, Class<Data> dataClass,
            ModelLoaderFactory<Model, Data> factory) {
        add(modelClass, dataClass, factory, false /*append*/);
    }

    private <Model, Data> void add(Class<Model> modelClass, Class<Data> dataClass,
            ModelLoaderFactory<Model, Data> factory, boolean append) {
        Entry<Model, Data> entry = new Entry<Model, Data>(modelClass, dataClass, factory);
        entries.add(append ? entries.size() : 0, entry);
    }

    <Model, Data> List<ModelLoaderFactory<Model, Data>> replace(Class<Model> modelClass, Class<Data> dataClass,
            ModelLoaderFactory<Model, Data> factory) {
        List<ModelLoaderFactory<Model, Data>> removed = remove(modelClass, dataClass);
        append(modelClass, dataClass, factory);
        return removed;
    }

    <Model, Data> List<ModelLoaderFactory<Model, Data>> remove(Class<Model> modelClass, Class<Data> dataClass) {
        List<ModelLoaderFactory<Model, Data>> factories = new ArrayList<ModelLoaderFactory<Model, Data>>();
        for (Iterator<Entry<?, ?>> iterator = entries.iterator(); iterator.hasNext();) {
            Entry<?, ?> entry = iterator.next();
            if (entry.handles(modelClass, dataClass)) {
                iterator.remove();
                factories.add(getFactory(modelClass, dataClass, entry));
            }
        }
        return factories;
    }

    <Model> List<ModelLoader<Model, ?>> build(Class<Model> modelClass) {
        List<ModelLoader<Model, ?>> loaders = new ArrayList<ModelLoader<Model, ?>>();
        for (Entry<?, ?> entry : entries) {
            if (entry.handles(modelClass)) {
                loaders.add(build(modelClass, entry));
            }
        }
        return loaders;
    }

    public <Model, Data> List<ModelLoader<Model, Data>> build(Class<Model> modelClass, Class<Data> dataClass) {
        List<ModelLoader<Model, Data>> loaders = new ArrayList<ModelLoader<Model, Data>>();
        for (Entry<?, ?> entry : entries) {
            if (entry.handles(modelClass, dataClass)) {
                loaders.add(build(modelClass, dataClass, entry));
            }
        }
        return loaders;
    }

    @SuppressWarnings("unchecked")
    private <Model, Data> ModelLoaderFactory<Model, Data> getFactory(Class<Model> modelClass, Class<Data> dataClass,
            Entry<?, ?> entry) {
        return (ModelLoaderFactory<Model, Data>) entry.factory;
    }

    @SuppressWarnings("unchecked")
    private <Model> ModelLoader<Model, ?> build(Class<Model> modelClass, Entry<?, ?> entry) {
        return (ModelLoader<Model, ?>) Preconditions.checkNotNull(entry.factory.build(context, this));
    }

    @SuppressWarnings("unchecked")
    private <Model, Data> ModelLoader<Model, Data> build(Class<Model> modelClass, Class<Data> dataClass,
            Entry<?, ?> entry) {
        return (ModelLoader<Model, Data>) Preconditions.checkNotNull(entry.factory.build(context, this));
    }

    private static class Entry<Model, Data> {
        private final Class<Model> modelClass;
        private final Class<Data> dataClass;
        private final ModelLoaderFactory<Model, Data> factory;

        public Entry(Class<Model> modelClass, Class<Data> dataClass, ModelLoaderFactory<Model, Data> factory) {
            this.modelClass = modelClass;
            this.dataClass = dataClass;
            this.factory = factory;
        }

        public boolean handles(Class<?> modelClass, Class<?> dataClass) {
            return handles(modelClass) && this.dataClass.isAssignableFrom(dataClass);
        }

        public boolean handles(Class<?> modelClass) {
            return this.modelClass.isAssignableFrom(modelClass);
        }
    }
}