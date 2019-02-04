# book-store-app
Book Store 
	-- Name 
	-- Address
	
Book model 

	-- Name
	-- Author
	-- Publisher
	-- Edition
	-- Genre 
	-- Segment/Type
	-- Price
	-- Quantity
	-- ISBN
	
REST operations: 
Book Store Application

Brief : Book Store application that supports addition of a new book store. 
Book Store API:
1. Add a book store
	API reference 
	-- POST <URL>/bookstore/

Book level operations include :
 1. Add multiple books with respect to each book store (both sequential and parallel file upload supported 
 	API reference: 
		-- POST <URL>/book/sequential 
		-- POST <URL>/book/parallel	
	Note : After saving into the database , file is dumped to a S3 bucket
	
 2. Update individual book details 
 	API reference:
		-- PUT <URL>/book/
	
 3. View a book's details ( Get book details by ISBN )
 	API reference: 
		-- GET <URL>/book/{isbn}
	
 4. Remove a book from the book store ( Remove book from inventory by ISBN ) 
 	API reference: 
		-- DELETE <URL>/book/{isbn}
	
 5. Purchase of a book 
	API reference: 
		-- POST <URL>/book/purchase
	Request Body : 
		{
			"bookname" : "",
			"author"   : "",
			"edition"  : 
		}
	
  6. Search book 
 	API reference
		-- GET <URL>/book/searchby/bookname/{name}/author/{author} 
		(by name and author)
 
Assumption : Each book is uniquely identified by a combination of it's name , author and ISBN number.
