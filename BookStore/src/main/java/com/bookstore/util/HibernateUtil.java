package com.bookstore.util;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bookstore.model.Book;

@Component
@SuppressWarnings("unchecked")
public class HibernateUtil<T> {
	@Autowired
	EntityManager entityManager;
	
	public T save(T object) {
		Session session=entityManager.unwrap(Session.class);
		return (T) session.save(object);
	}
	
	public void saveOrUpdate(T object,String entity) {
		Session session=entityManager.unwrap(Session.class);
		 session.saveOrUpdate(entity,object);
	}
	public void update(T object) {
		Session session=entityManager.unwrap(Session.class);
		 session.update(object);
	}
	
	public Query<T> createQuery(String query)
	{
		Session session = entityManager.unwrap(Session.class);
		return session.createQuery(query);
	}
	
	@Transactional
	public Query<T> select(String query) {
		Session sesson=entityManager.unwrap(Session.class);
		return sesson.createQuery(query);
	}
	
	@Transactional
	public Book getBook(Serializable value) {
		Session session=entityManager.unwrap(Session.class);
		return session.get(Book.class, value);
	}
	
}
