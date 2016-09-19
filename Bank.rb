require 'sinatra'
require 'slim'

require './models/all.rb'

use Rack::Session::Cookie, :key => 'top_bank',
                           :path => '/',
                           :secret => 'alaposanOrzottBankTitok'

before do
    user    = ENV['BANK_DB_USER'] || 'akos'
    pass    = ENV['BANK_DB_PASS'] || 'jelszo'
    db_name = ENV['BANK_DB_NAME'] || 'bank'
    r = DBModel.connect(user, pass, 'localhost', db_name)
end

helpers do
    def get_user
        @user ||= Ugyfel.find_by_id(session[:ugyfel_id])
        if @user == [] or @user == nil
            @user = nil
        elsif @user.class == Array
            @user = @user.first
        end
        return @user
    end

    def logged_in?
        get_user() != nil 
    end

end

get '/' do
    slim :index, layout: :main
end

get '/belepes' do
    slim :belepes, layout: :main
end

get '/belepes/hibas' do
    slim :belepes, layout: :main, locals: { hibas: true }
end

post '/belepes' do
    u = Ugyfel.belep params
    if (u.nil? or u == false or u == [])
        redirect('/belepes/hibas')
    else
        session[:ugyfel_id] = u.first.id
        redirect('/ugyfel')
        #slim :ugyfel, locals: { ugyfel: u.first}, layout: :main
    end
end

get '/fiokok' do
    slim :fiokok, locals: { fiokok: Fiok.all }, layout: :main
end

get '/ugyfel' do
    if !logged_in?
        redirect '/belepes'
    else
        @szamlak = get_user().szamlak
        slim :ugyfel, locals: { ugyfel: get_user(), szamlak: @szamlak }, layout: :main
    end
end

get '/szamlak' do
    if !logged_in?
        redirect '/belepes'
    else
        slim :szamlak, locals: { szamlak: get_user().szamlak }, layout: :main
    end
end

get '/szamlak/:id' do |id|
    sz = Szamla.find_by_owner(id)
    slim :szamlak, locals: { szamlak: sz }, layout: :main
end

get '/kijelentkezes' do
    session.delete(:ugyfel_id)
    redirect '/'
end

get '/tranzakcio/uj' do
    if !logged_in?
        redirect '/belepes'
    else
        slim :uj_tranzakcio, layout: :main, locals: { ugyfel: get_user() }
    end
end

post '/trazakcio/uj' do 
    
end
