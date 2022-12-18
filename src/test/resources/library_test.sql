-- MySQL dump 10.13  Distrib 8.0.29, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: library_test
-- ------------------------------------------------------
-- Server version	8.0.31-0ubuntu0.22.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `author`
--

DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `author` (
                          `id` int NOT NULL AUTO_INCREMENT,
                          `name` varchar(45) NOT NULL,
                          `surname` varchar(45) NOT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=469 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
--
-- Table structure for table `author_ua`
--

DROP TABLE IF EXISTS `author_ua`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `author_ua` (
                             `author_id` int NOT NULL AUTO_INCREMENT,
                             `name` varchar(45) NOT NULL,
                             `surname` varchar(45) NOT NULL,
                             PRIMARY KEY (`author_id`),
                             CONSTRAINT `author_ua_author_id_fk` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=464 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `author_ua`
--

LOCK TABLES `author_ua` WRITE;
/*!40000 ALTER TABLE `author_ua` DISABLE KEYS */;
/*!40000 ALTER TABLE `author_ua` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `title` varchar(45) NOT NULL,
                        `author_id` int NOT NULL,
                        `publisher_isbn` varchar(17) NOT NULL,
                        `date` date DEFAULT NULL,
                        `description` varchar(500) NOT NULL DEFAULT 'No info',
                        `cover_id` int DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        KEY `fk_book_author_idx` (`author_id`),
                        KEY `fk_book_publisher_isbn` (`publisher_isbn`),
                        KEY `fk_book_cover_id_idx` (`cover_id`),
                        CONSTRAINT `fk_book_author` FOREIGN KEY (`author_id`) REFERENCES `author` (`id`),
                        CONSTRAINT `fk_book_cover_id` FOREIGN KEY (`cover_id`) REFERENCES `cover` (`id`),
                        CONSTRAINT `fk_book_publisher_isbn` FOREIGN KEY (`publisher_isbn`) REFERENCES `publisher` (`isbn`)
) ENGINE=InnoDB AUTO_INCREMENT=229 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `book_has_genre`
--

DROP TABLE IF EXISTS `book_has_genre`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_has_genre` (
                                  `book_id` int NOT NULL,
                                  `genre_id` int NOT NULL,
                                  PRIMARY KEY (`book_id`,`genre_id`),
                                  KEY `fk_book_has_genre_genre1_idx` (`genre_id`),
                                  KEY `fk_book_has_genre_book1_idx` (`book_id`),
                                  CONSTRAINT `fk_book_has_genre_book1` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
                                  CONSTRAINT `fk_book_has_genre_genre1` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_has_genre`
--

LOCK TABLES `book_has_genre` WRITE;
/*!40000 ALTER TABLE `book_has_genre` DISABLE KEYS */;
/*!40000 ALTER TABLE `book_has_genre` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_ua`
--

DROP TABLE IF EXISTS `book_ua`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_ua` (
                           `book_id` int NOT NULL AUTO_INCREMENT,
                           `title` varchar(45) NOT NULL,
                           `author_ua_id` int NOT NULL,
                           `publisher_isbn` varchar(17) NOT NULL,
                           `date` date NOT NULL,
                           `description` varchar(500) NOT NULL,
                           `cover_id` int DEFAULT NULL,
                           PRIMARY KEY (`book_id`),
                           KEY `fk_book_ua_1_idx` (`author_ua_id`),
                           KEY `fk_book_ua_2_idx` (`publisher_isbn`),
                           KEY `fk_book_ua_1_idx1` (`cover_id`),
                           CONSTRAINT `book_ua_book_id_fk` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
                           CONSTRAINT `fk_book_ua_author_ua` FOREIGN KEY (`author_ua_id`) REFERENCES `author_ua` (`author_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                           CONSTRAINT `fk_book_ua_cover_id` FOREIGN KEY (`cover_id`) REFERENCES `cover` (`id`),
                           CONSTRAINT `fk_book_ua_publisher_isbn` FOREIGN KEY (`publisher_isbn`) REFERENCES `publisher` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_ua`
--

LOCK TABLES `book_ua` WRITE;
/*!40000 ALTER TABLE `book_ua` DISABLE KEYS */;
/*!40000 ALTER TABLE `book_ua` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_ua_has_genre_ua`
--

DROP TABLE IF EXISTS `book_ua_has_genre_ua`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_ua_has_genre_ua` (
                                        `book_ua_id` int NOT NULL,
                                        `genre_ua_id` int NOT NULL,
                                        PRIMARY KEY (`book_ua_id`,`genre_ua_id`),
                                        KEY `fk_genre_ua_id_idx` (`genre_ua_id`),
                                        CONSTRAINT `fk_book_ua_id` FOREIGN KEY (`book_ua_id`) REFERENCES `book_ua` (`book_id`),
                                        CONSTRAINT `fk_genre_ua_id` FOREIGN KEY (`genre_ua_id`) REFERENCES `genre_ua` (`genre_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_ua_has_genre_ua`
--

LOCK TABLES `book_ua_has_genre_ua` WRITE;
/*!40000 ALTER TABLE `book_ua_has_genre_ua` DISABLE KEYS */;
/*!40000 ALTER TABLE `book_ua_has_genre_ua` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cover`
--

DROP TABLE IF EXISTS `cover`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cover` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `img` blob NOT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cover`
--

LOCK TABLES `cover` WRITE;
/*!40000 ALTER TABLE `cover` DISABLE KEYS */;
/*!40000 ALTER TABLE `cover` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `genre`
--

DROP TABLE IF EXISTS `genre`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `genre` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `name` varchar(45) NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `name_UNIQUE` (`id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1497 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `genre_ua`
--

DROP TABLE IF EXISTS `genre_ua`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `genre_ua` (
                            `genre_id` int NOT NULL,
                            `name` varchar(45) NOT NULL,
                            PRIMARY KEY (`genre_id`),
                            CONSTRAINT `fk_genre_ua_genre_id` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `genre_ua`
--

LOCK TABLES `genre_ua` WRITE;
/*!40000 ALTER TABLE `genre_ua` DISABLE KEYS */;
/*!40000 ALTER TABLE `genre_ua` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `publisher`
--

DROP TABLE IF EXISTS `publisher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `publisher` (
                             `isbn` varchar(17) NOT NULL,
                             `name` varchar(45) NOT NULL,
                             PRIMARY KEY (`isbn`),
                             UNIQUE KEY `name_UNIQUE` (`name`),
                             UNIQUE KEY `isbn_UNIQUE` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reader`
--

DROP TABLE IF EXISTS `reader`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reader` (
                          `user_id` int NOT NULL,
                          `blocked` tinyint NOT NULL,
                          PRIMARY KEY (`user_id`),
                          CONSTRAINT `fk_reader_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reader`
--

LOCK TABLES `reader` WRITE;
/*!40000 ALTER TABLE `reader` DISABLE KEYS */;
/*!40000 ALTER TABLE `reader` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reader_has_receipt`
--

DROP TABLE IF EXISTS `reader_has_receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reader_has_receipt` (
                                      `reader_Id` int NOT NULL,
                                      `receipt_id` int NOT NULL,
                                      PRIMARY KEY (`reader_Id`,`receipt_id`),
                                      KEY `fk_reader_has_receipt_receipt1_idx` (`receipt_id`),
                                      KEY `fk_reader_has_receipt_reader1_idx` (`reader_Id`),
                                      CONSTRAINT `fk_reader_has_receipt_reader_id` FOREIGN KEY (`reader_Id`) REFERENCES `reader` (`user_id`),
                                      CONSTRAINT `fk_reader_has_receipt_receipt1` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reader_has_receipt`
--

LOCK TABLES `reader_has_receipt` WRITE;
/*!40000 ALTER TABLE `reader_has_receipt` DISABLE KEYS */;
/*!40000 ALTER TABLE `reader_has_receipt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt`
--

DROP TABLE IF EXISTS `receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt` (
                           `id` int NOT NULL AUTO_INCREMENT,
                           `reader_id` int NOT NULL,
                           `time` datetime NOT NULL,
                           PRIMARY KEY (`id`),
                           KEY `fk_receipt_1_idx` (`reader_id`),
                           CONSTRAINT `fk_receipt_reader_id` FOREIGN KEY (`reader_id`) REFERENCES `reader` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=238 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt`
--

LOCK TABLES `receipt` WRITE;
/*!40000 ALTER TABLE `receipt` DISABLE KEYS */;
/*!40000 ALTER TABLE `receipt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt_has_book`
--

DROP TABLE IF EXISTS `receipt_has_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt_has_book` (
                                    `receipt_id` int NOT NULL,
                                    `book_id` int NOT NULL,
                                    PRIMARY KEY (`receipt_id`,`book_id`),
                                    KEY `fk_receipt_has_book_book1_idx` (`book_id`),
                                    KEY `fk_receipt_has_book_receipt1_idx` (`receipt_id`),
                                    CONSTRAINT `fk_receipt_has_book_book1` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
                                    CONSTRAINT `fk_receipt_has_book_receipt1` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt_has_book`
--

LOCK TABLES `receipt_has_book` WRITE;
/*!40000 ALTER TABLE `receipt_has_book` DISABLE KEYS */;
/*!40000 ALTER TABLE `receipt_has_book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `name` varchar(45) NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock`
--

DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock` (
                         `book_id` int NOT NULL,
                         `count` int unsigned NOT NULL,
                         KEY `fk_stock_book1_idx` (`book_id`),
                         CONSTRAINT `fk_stock_book1` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock`
--

LOCK TABLES `stock` WRITE;
/*!40000 ALTER TABLE `stock` DISABLE KEYS */;
/*!40000 ALTER TABLE `stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription` (
                                `id` int NOT NULL AUTO_INCREMENT,
                                `reader_id` int NOT NULL,
                                `book_id` int NOT NULL,
                                `taken_date` date NOT NULL,
                                `brought_date` date NOT NULL,
                                `fine` decimal(10,0) NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `fk_loans_book1_idx` (`book_id`),
                                KEY `fk_subscription_1_idx` (`reader_id`),
                                CONSTRAINT `fk_subscription_book_id` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
                                CONSTRAINT `fk_subscription_reader_id` FOREIGN KEY (`reader_id`) REFERENCES `reader` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=190 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `login` varchar(45) NOT NULL,
                        `password_hash` varchar(256) NOT NULL,
                        `role_id` int NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `login_UNIQUE` (`login`),
                        KEY `fk_user_1_idx` (`role_id`,`id`),
                        CONSTRAINT `fk_user_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-12-18 11:53:49