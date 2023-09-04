package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        userPlaylistMap.put(user, new ArrayList<>()); //Create key from the start to avoid null pointer
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist= new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist, new ArrayList<>()); //Create key from the start to avoid null pointer
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album = new Album(title);
        albums.add(album);
        for (Artist artist : artists)
        {
            String name =artist.getName();
            if(name.equals(artistName)){
                List<Album> albumList = artistAlbumMap.getOrDefault(artistName,new ArrayList<>());
                albumList.add(album);
                artistAlbumMap.put(artist,albumList);
                albumSongMap.put(album, new ArrayList<>()); //Create key from the start to avoid null pointer
                return album;
            }
        }
        Artist artist = new Artist(artistName);
        artists.add(artist);
        List<Album> albumList = artistAlbumMap.getOrDefault(artistName,new ArrayList<>());
        albumList.add(album);
        artistAlbumMap.put(artist,albumList);
        albumSongMap.put(album, new ArrayList<>()); //Create key from the start to avoid null pointer
        return album;

    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Song song = new Song(title,length);
        songs.add(song);

        Album currAlbum = null;
        for(Album album : albums){
            if(album.getTitle().equals(albumName)){
                currAlbum=album;
                break;
            }
        }
        if(currAlbum==null){
            throw new Exception("Album does not exist");
        }

        //add song to album song mapping
        List<Song> songList = albumSongMap.getOrDefault(currAlbum,new ArrayList<>());
        songList.add(song);
        albumSongMap.put(currAlbum,songList);

        //add song as a key of songLikeMap
        songLikeMap.put(song, new ArrayList<>());

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user=user1;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }
        //create new playlist and add in playlists list
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        playlistSongMap.put(playlist,new ArrayList<>());

        for(Song song : songs){
            if(song.getLength()==length){
                playlistSongMap.get(playlist).add(song);
            }
        }

        userPlaylistMap.get(user).add(playlist);

        //add user-playlist pair in creatorPlayListMap
        creatorPlaylistMap.put(user, playlist);

        //add playlist as a key to playlistListenerMap
        List<User> userList=playlistListenerMap.getOrDefault(playlist,new ArrayList<>());
        userList.add(user);
        playlistListenerMap.put(playlist,userList);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user =null;
        for(User user1 : users){
            if(user1.getMobile().equals(mobile)){
                user=user1;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }

        //create playlist and add it to playlists list
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        //add playlist as key of playlistSongMap
        playlistSongMap.put(playlist, new ArrayList<>());

        //add all the songs according to condition
        for(String songTitle: songTitles){
            for(Song song: songs){
                if(song.getTitle().equals(songTitle)){
                    playlistSongMap.get(playlist).add(song);
                }
            }
        }

        creatorPlaylistMap.put(user,playlist);
        userPlaylistMap.get(user).add(playlist);

        //add playlist as a key to playlistListenerMap
        playlistListenerMap.put(playlist, new ArrayList<>());
        //add user to playlistListerMap
        playlistListenerMap.get(playlist).add(user);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //checking user
        User currUser = null;
        for(User user: users){
            if(user.getMobile().equals(mobile)){
                currUser = user;
            }
        }
        if(currUser==null){
            throw new Exception("User does not exist");
        }

        //checking playlist
        Playlist playlist = null;
        for(Playlist playlist1 : playlists){
            if(playlist1.getTitle().equals(playlistTitle)){
                playlist=playlist1;
                break;
            }
        }
        if(playlist==null){
            throw  new Exception("Playlist does not exist");
        }


        //if user is a creator, directly return currPlaylist
        if(creatorPlaylistMap.containsKey(currUser)){
            return playlist;
        }

        //add playlist against currUser in userPlaylistMap
        userPlaylistMap.get(currUser).add(playlist);

        //add user to playlistListerMap
        if(playlistListenerMap.containsKey(playlist)){
            if(!playlistListenerMap.get(playlist).contains(currUser)){
                playlistListenerMap.get(playlist).add(currUser);
            }
        }
        return playlist;

    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User currUser = null;
        for(User user: users){
            if(user.getMobile().equals(mobile)){
                currUser = user;
            }
        }
        if(currUser==null){
            throw new Exception("User does not exist");
        }

        Song currSong = null;
        for(Song song:songs){
            if(song.getTitle().equals(songTitle)){
                currSong=song;
                break;
            }
        }
        if(currSong==null){
            throw new Exception("Song does not exist");
        }

        if(songLikeMap.get(currSong).contains(currUser)){
            return currSong;
        }

        //add users to songLikeMap against currSong
        songLikeMap.get(currSong).add(currUser);

        //increase like count for a song
        currSong.setLikes(currSong.getLikes()+1);

        //now we need to find an artist and increase the like count for artist,
        //for that we will first need to find album of that artist
        Album currAlbum = null;
        for(Album album: albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(currSong)){
                currAlbum = album;
            }
        }
        //now we have album of the artist and we can find artist
        Artist currArtist = null;
        for(Artist artist: artistAlbumMap.keySet()){
            if(artistAlbumMap.get(artist).contains(currAlbum)){
                currArtist = artist;
            }
        }
        if (currArtist==null){
            throw new Exception("");
        }

        //increase like count of an artist
        currArtist.setLikes(currArtist.getLikes()+1);
        return  currSong;

    }

    public String mostPopularArtist() {
        //check artist's like count in artists list and return the artist with max like count
        Artist currArtist = null;
        int maxLikes = 0;

        for(Artist artist: artists){
            if(artist.getLikes()>=maxLikes){
                currArtist = artist;
                maxLikes = artist.getLikes();
            }
        }
        if(currArtist==null){
            return "";
        }
        return currArtist.getName();
    }

    public String mostPopularSong() {
        //check song's like count in songs list and return the song with max like count
        Song currSong = null;
        int maxLikes = 0;

        for(Song song: songs){
            if(song.getLikes()>=maxLikes){
                currSong = song;
                maxLikes = song.getLikes();
            }
        }
        if(currSong==null){
            return "";
        }
        return currSong.getTitle();
    }
}
