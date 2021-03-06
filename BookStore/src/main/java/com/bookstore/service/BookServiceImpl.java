package com.bookstore.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.bookstore.dao.IBookDAO;
import com.bookstore.exception.BookAlreadyExist;
import com.bookstore.exception.BookNotFoundException;
import com.bookstore.exception.InvalidTokenOrExpiredException;
import com.bookstore.exception.UserDoesNotExistException;
import com.bookstore.model.Book;
import com.bookstore.model.UserData;
import com.bookstore.response.BookResponse;
import com.bookstore.util.JwtTokenUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookServiceImpl implements IBookService {

	@Autowired
	IBookDAO bookdao;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	private JwtTokenUtil generateToken;

	private static String UPLOADED_FOLDER = "G:/BookStore-FrontEnd/BookStoreFrontEnd/src/assets/BookImages/";
//	G:\BookStore-FrontEnd\BookStoreFrontEnd
	static int userId;

	@Override
	public ResponseEntity<BookResponse> addBook(Book book, String token) {
		if (verifyUser(token)) {
			String bookName = book.getBookName().toLowerCase().trim();
			if (bookdao.getBookByName(bookName) == null) {
				book.setUserId(userId);
				bookdao.addBook(book);
				return ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(new BookResponse(book, 202, book.getBookName() + " Book Added"));
			} else {
				throw new BookAlreadyExist();
			}
		} else {
			throw new UserDoesNotExistException("User Does Not Exist", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<BookResponse> removeBook(int id, String token) {
		if (verifyUser(token)) {
			log.info("book data" + bookdao.getBookByBookId(id));
			if (bookdao.getBookByBookId(id) != null) {
				if (bookdao.deleteBook(id) > 0)
					return ResponseEntity.status(HttpStatus.ACCEPTED)
							.body(new BookResponse(202, bookdao.getBookByBookId(id).getBookName() + " Deleted Successfully"));
				else
					return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
							.body(new BookResponse(502, bookdao.getBookByBookId(id).getBookName() + " Could Not delete Please Try Again"));
			} else {
				throw new BookNotFoundException();
			}
		} else {
			throw new UserDoesNotExistException("User Does Not Exist", HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<BookResponse> getAllBooks(String token) {
		if (verifyUser(token)) {
			if (bookdao.getAllBooks() != null) {
				return ResponseEntity.status(HttpStatus.ACCEPTED).body(new BookResponse(202,
						"Total Books Are:" + bookdao.getAllBooks().size(), bookdao.getAllBooks()));
			} else {
				throw new BookNotFoundException();
			}
		} else {
			throw new UserDoesNotExistException("User Does Not Exist", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<BookResponse> getSellerBooks(String token) {
		if (verifyUser(token)) {
			if (bookdao.getAllBooks() != null) {
				List<Book> books = bookdao.getAllBooks().stream().filter(p -> p.getUserId() == userId)
						.collect(Collectors.toList());

				return ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(new BookResponse(202, "Total Books Are:" + books.size(), books));
			} else {
				throw new BookNotFoundException();
			}
		} else {
			throw new UserDoesNotExistException("User Does Not Exist", HttpStatus.BAD_REQUEST);
		}
	}

	
	@Override
	public ResponseEntity<BookResponse> updateBookDetails(String bookName, Book updatedBook, String token) {
		if (verifyUser(token)) {
			if (bookdao.getBookByName(updatedBook.getBookName()) != null) {
				bookdao.updateBook(updatedBook, bookName);
				return ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(new BookResponse(updatedBook, 202, bookName + " Updated"));
			} else {
				throw new BookNotFoundException();
			}
		} else {
			throw new UserDoesNotExistException("User Does Not Exist", HttpStatus.BAD_REQUEST);
		}
	}

	public boolean verifyUser(String token) {
		log.info("-------->>>>>>>>>>>>>Calling USerApi From NotesApi<<<<<<<<<<<<<<<<--------------------");
		UserData userData = restTemplate.getForObject("http://localhost:8092/users/" + token, UserData.class);
		log.info("--------->>>>>>>>>>>>Accessing DataFrom UserApi<<<<<<<<<<<---------------------");
		try {
			log.info("verifyUserApi Using RestTemplate From UserApi Success--------->:"
					+ (userData.getUId() == generateToken.parseToken(token)));
			userId = userData.getUId();
			return (userData.getUId() == generateToken.parseToken(token));
		} catch (SignatureVerificationException | JWTDecodeException | AlgorithmMismatchException e) {
			throw new InvalidTokenOrExpiredException("Invalid Token or Token Expired", HttpStatus.BAD_REQUEST);
		}
	}
	
	
	 @Override
	  public void saveBookImage(MultipartFile file,int bookId,String token) {
	    try {
	    	byte[] bytes = file.getBytes();
	    	Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
	    	Files.write(path, bytes);
	    	bookdao.uploadImage(bookId, "assets/BookImages/"+file.getOriginalFilename());
	    } catch (Exception e) {
	      throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
	    }
	  }

}
