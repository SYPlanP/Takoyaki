package pe.chalk.takoyaki.filter;

import pe.chalk.takoyaki.Target;
import pe.chalk.takoyaki.data.Data;

/**
 * @author ChalkPE <amato0617@gmail.com>
 * @since 2015-04-16
 */
public abstract class ContentFilter<T extends Data> extends Filter<T> {
    public ContentFilter(Target target){
        super(target);
    }
}