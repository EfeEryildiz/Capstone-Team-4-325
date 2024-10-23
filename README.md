# Flashcard Study Tool

### Team Members:
- Efe Eryildiz
- Steven Buruca
- Omar Shaker
- Richard Sheridan

## Overview
The Flashcard Study Tool is a Java-based application designed to help users create, manage, and study flashcards. The tool includes Firebase integration for user authentication and real-time data storage, allowing users to securely save their flashcards and access them across devices.

## Features
- **Create Flashcards**: Add, edit, or delete flashcards organized into decks.
- **Quiz Mode**: Test knowledge with randomized flashcards.
- **Firebase Integration**: Users can sign in, and flashcard data is stored securely in Firebase.
- **Progress Tracking**: View performance during quizzes and track learning progress across sessions.

## World Assumptions
Users require a digital flashcard app with secure cloud storage, allowing access across devices. They expect a responsive and interactive user experience, and the app assumes access to the internet for Firebase services.

## User Requirements
- **Authentication**: Users must sign in via Firebase for secure access to their flashcards.
- **Flashcard Management**: Create, edit, and delete flashcard sets.
- **Data Sync**: Flashcards must be stored in Firebase, allowing for cross-device accessibility.
- **Customization**: Users can manage multiple flashcard decks and track quiz progress.
- **Data Persistence**: All flashcards and progress data are stored securely in Firebase’s real-time database.

## Specifications & Interface
- **UI**: Built using JavaFX for the graphical interface. Firebase handles back-end data storage and user authentication.
- **Screens**:
  - **Main Dashboard**: Manage flashcard sets.
  - **Flashcard Creation Screen**: Create and edit flashcards.
  - **Quiz Mode**: Randomly presents flashcards for quizzing.
- **Firebase Features**:
  - **Authentication**: Users sign up and log in with Firebase Authentication.
  - **Database**: Firebase Realtime Database stores flashcards, which are accessible across devices.
  
## Program & Hardware Requirements
- **Development**: Built using Java, JavaFX, and Firebase.
- **Firebase SDK**: Used for Authentication and Realtime Database.
- **Hardware**: Runs on any PC that supports Java.
- **Tools**:
  - **IntelliJ IDEA** for development.
  - **Firebase Console** for managing authentication and database.
  - **JavaFX SceneBuilder** for interface design.
