package com.thinkbiganalytics.metadata.jpa.feedmgr.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.jpa.BaseJpaProvider;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.NoResultException;

/**
 * Created by sr186054 on 5/3/16.
 */
public class JpaFeedManagerCategoryProvider extends BaseJpaProvider<FeedManagerCategory, Category.ID> implements FeedManagerCategoryProvider {

    @Override
    public Class<? extends FeedManagerCategory> getEntityClass() {
        return JpaFeedManagerCategory.class;
    }
    @Override
    public FeedManagerCategory findBySystemName(String systemName) {

        FeedManagerCategory category =  null;
        try {
            category = (FeedManagerCategory) entityManager.createNamedQuery(FeedManagerNamedQueries.CATEGORY_FIND_BY_SYSTEM_NAME)
                    .setParameter("systemName", systemName)
                    .getSingleResult();
        }catch(NoResultException e){
            category =  null;
        }
        return category;
    }

    @Override
    public FeedManagerCategory ensureCategory(String systemName) {
        FeedManagerCategory c = findBySystemName(systemName);
        if (c == null) {
            JpaFeedManagerCategory cat = new JpaFeedManagerCategory(systemName);
            c = create(cat);
        }
        return c;


    }

    @Override
    public Category.ID resolveId(Serializable fid) {
        return new JpaFeedManagerCategory.CategoryId(fid);
    }

    @Override
    public Set<UserFieldDescriptor> getUserFields() {
        return null;
    }

    @Override
    public void setUserFields(Set<UserFieldDescriptor> userFields) {

    }

    @Override
    public Set<UserFieldDescriptor> getFeedUserFields(Category.ID categoryId) {
        return null;
    }

    @Override
    public void setFeedUserFields(Category.ID categoryId, Set<UserFieldDescriptor> userFields) {

    }
}
